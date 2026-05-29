package com.inventario.service;

import com.inventario.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AlertaEmailService {

    private static final Logger log = LoggerFactory.getLogger(AlertaEmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${app.alertas.destinatarios}")
    private String destinatariosRaw;

    private String[] getDestinatarios() {
        return destinatariosRaw.split(",");
    }

    @Async
    public void enviarAlertaStockBajo(Producto producto) {
        log.info("[AlertaEmail] Intentando enviar alerta para producto: {} (stock={}, minimo={})",
                producto.getNombre(), producto.getStockActual(), producto.getStockMinimo());
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(getDestinatarios());
            helper.setSubject("⚠️ StockWise — Alerta de Stock Bajo: " + producto.getNombre());

            String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#0a0a0a;color:#e5e5e5;border-radius:12px;overflow:hidden;">
                    <div style="background:#00a88a;padding:24px 32px;">
                        <h1 style="margin:0;font-size:22px;color:#000;">⬡ StockWise</h1>
                        <p style="margin:4px 0 0;font-size:13px;color:#003d33;">Sistema de Gestión de Inventario</p>
                    </div>
                    <div style="padding:32px;">
                        <h2 style="color:#f87171;margin:0 0 20px;">⚠️ Alerta de Stock Bajo</h2>
                        <p style="color:#aaa;margin:0 0 24px;">Se ha detectado que el siguiente producto está por debajo del nivel mínimo de stock:</p>

                        <table style="width:100%%;border-collapse:collapse;background:#111;border-radius:8px;overflow:hidden;">
                            <tr>
                                <td style="padding:12px 16px;color:#aaa;font-size:13px;border-bottom:1px solid #222;">Producto</td>
                                <td style="padding:12px 16px;font-weight:600;border-bottom:1px solid #222;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding:12px 16px;color:#aaa;font-size:13px;border-bottom:1px solid #222;">Código</td>
                                <td style="padding:12px 16px;border-bottom:1px solid #222;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding:12px 16px;color:#aaa;font-size:13px;border-bottom:1px solid #222;">Stock Actual</td>
                                <td style="padding:12px 16px;border-bottom:1px solid #222;color:#f87171;font-weight:700;font-size:18px;">%d unidades</td>
                            </tr>
                            <tr>
                                <td style="padding:12px 16px;color:#aaa;font-size:13px;border-bottom:1px solid #222;">Stock Mínimo</td>
                                <td style="padding:12px 16px;border-bottom:1px solid #222;">%d unidades</td>
                            </tr>
                            <tr>
                                <td style="padding:12px 16px;color:#aaa;font-size:13px;">Categoría</td>
                                <td style="padding:12px 16px;">%s</td>
                            </tr>
                        </table>

                        <div style="margin-top:24px;padding:16px;background:#1a0a0a;border-left:3px solid #f87171;border-radius:4px;">
                            <p style="margin:0;color:#f87171;font-size:14px;">
                                🚨 Se recomienda realizar una orden de compra o ajuste de inventario a la brevedad posible.
                            </p>
                        </div>

                        <p style="margin-top:24px;color:#555;font-size:12px;">Alerta generada automáticamente el %s por StockWise.</p>
                    </div>
                </div>
                """.formatted(
                    producto.getNombre(),
                    producto.getCodigo() != null ? producto.getCodigo() : "—",
                    producto.getStockActual(),
                    producto.getStockMinimo(),
                    producto.getCategoria() != null ? producto.getCategoria().getNombre() : "—",
                    fecha
                );

            helper.setText(html, true);
            mailSender.send(mensaje);
            log.info("[AlertaEmail] ✅ Alerta enviada correctamente para: {}", producto.getNombre());

        } catch (Exception e) {
            log.error("[AlertaEmail] ❌ Error enviando alerta para '{}': {}", producto.getNombre(), e.getMessage(), e);
        }
    }

    @Async
    public void enviarResumenStockBajo(List<Producto> productosEnAlerta) {
        if (productosEnAlerta.isEmpty()) return;

        log.info("[AlertaEmail] Enviando resumen de {} productos con stock bajo", productosEnAlerta.size());
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(getDestinatarios());
            helper.setSubject("📋 StockWise — Resumen de Alertas de Stock ("
                + productosEnAlerta.size() + " productos)");

            StringBuilder filas = new StringBuilder();
            for (Producto p : productosEnAlerta) {
                String estado = p.getStockActual() == 0 ? "🔴 Agotado" : "🟡 Stock Bajo";
                filas.append("""
                    <tr>
                        <td style="padding:10px 14px;border-bottom:1px solid #222;">%s</td>
                        <td style="padding:10px 14px;border-bottom:1px solid #222;color:#f87171;font-weight:600;">%d</td>
                        <td style="padding:10px 14px;border-bottom:1px solid #222;">%d</td>
                        <td style="padding:10px 14px;border-bottom:1px solid #222;">%s</td>
                    </tr>
                    """.formatted(p.getNombre(), p.getStockActual(), p.getStockMinimo(), estado));
            }

            String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            String html = """
                <div style="font-family:Arial,sans-serif;max-width:700px;margin:0 auto;background:#0a0a0a;color:#e5e5e5;border-radius:12px;overflow:hidden;">
                    <div style="background:#00a88a;padding:24px 32px;">
                        <h1 style="margin:0;font-size:22px;color:#000;">⬡ StockWise</h1>
                        <p style="margin:4px 0 0;font-size:13px;color:#003d33;">Resumen de Alertas de Stock</p>
                    </div>
                    <div style="padding:32px;">
                        <h2 style="color:#f5c400;margin:0 0 8px;">📋 %d producto(s) requieren atención</h2>
                        <p style="color:#aaa;margin:0 0 24px;">Los siguientes productos están por debajo del nivel mínimo de stock:</p>
                        <table style="width:100%%;border-collapse:collapse;background:#111;border-radius:8px;overflow:hidden;">
                            <thead>
                                <tr style="background:#1a1a1a;">
                                    <th style="padding:10px 14px;text-align:left;font-size:12px;color:#aaa;text-transform:uppercase;">Producto</th>
                                    <th style="padding:10px 14px;text-align:left;font-size:12px;color:#aaa;text-transform:uppercase;">Stock Actual</th>
                                    <th style="padding:10px 14px;text-align:left;font-size:12px;color:#aaa;text-transform:uppercase;">Mínimo</th>
                                    <th style="padding:10px 14px;text-align:left;font-size:12px;color:#aaa;text-transform:uppercase;">Estado</th>
                                </tr>
                            </thead>
                            <tbody>%s</tbody>
                        </table>
                        <p style="margin-top:24px;color:#555;font-size:12px;">Resumen generado el %s por StockWise.</p>
                    </div>
                </div>
                """.formatted(productosEnAlerta.size(), filas.toString(), fecha);

            helper.setText(html, true);
            mailSender.send(mensaje);
            log.info("[AlertaEmail] ✅ Resumen enviado correctamente ({} productos)", productosEnAlerta.size());

        } catch (Exception e) {
            log.error("[AlertaEmail] ❌ Error enviando resumen: {}", e.getMessage(), e);
        }
    }
}
