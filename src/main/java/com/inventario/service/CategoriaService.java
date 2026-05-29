package com.inventario.service;

import com.inventario.model.Categoria;
import com.inventario.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> findActivas() {
        return categoriaRepository.findByActivoTrue();
    }

    public Optional<Categoria> findById(Long id) {
        return categoriaRepository.findById(id);
    }

    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public void deleteById(Long id) {
        categoriaRepository.findById(id).ifPresent(c -> {
            c.setActivo(false);
            categoriaRepository.save(c);
        });
    }

    public long count() {
        return categoriaRepository.count();
    }
}