package com.inventario.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class VerificacionEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Async
    public void enviarCodigoVerificacion(String destinatario, String nombre, String codigo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("⬡ StockWise — Código de verificación");

            String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0;padding:0;background:#0f1117;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0f1117;padding:40px 16px;">
                    <tr><td align="center">
                      <table width="480" cellpadding="0" cellspacing="0"
                             style="background:#161b27;border:1px solid #2a2d3e;border-radius:16px;overflow:hidden;">
                        <!-- Header -->
                        <tr>
                          <td style="background:#161b27;padding:32px 40px 24px;border-bottom:1px solid #2a2d3e;text-align:center;">
                            <span style="font-size:28px;font-weight:800;color:#00ffb4;letter-spacing:-1px;">⬡ StockWise</span>
                          </td>
                        </tr>
                        <!-- Body -->
                        <tr>
                          <td style="padding:36px 40px;">
                            <p style="margin:0 0 8px;font-size:20px;font-weight:700;color:#e8eaf6;">
                              Hola, %s 👋
                            </p>
                            <p style="margin:0 0 28px;font-size:14px;color:#9094a6;line-height:1.6;">
                              Usa el siguiente código para verificar tu correo y activar tu cuenta.<br/>
                              <strong style="color:#e8eaf6;">Válido por 15 minutos.</strong>
                            </p>
                            <!-- Código -->
                            <div style="background:#0f1117;border:2px solid #00ffb4;border-radius:12px;
                                        padding:24px;text-align:center;margin-bottom:28px;">
                              <span style="font-size:42px;font-weight:800;letter-spacing:12px;
                                           color:#00ffb4;font-family:'Courier New',monospace;">%s</span>
                            </div>
                            <p style="margin:0;font-size:12px;color:#6b6f80;line-height:1.6;">
                              Si no creaste una cuenta en StockWise, puedes ignorar este correo.
                            </p>
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="padding:16px 40px;border-top:1px solid #2a2d3e;text-align:center;">
                            <span style="font-size:12px;color:#6b6f80;">
                              © 2024 StockWise — Sistema de Gestión de Inventario
                            </span>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(nombre, codigo);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando código de verificación: " + e.getMessage(), e);
        }
    }
}
