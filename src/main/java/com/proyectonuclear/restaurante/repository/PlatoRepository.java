package com.proyectonuclear.restaurante.repository;

import com.proyectonuclear.restaurante.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatoRepository extends JpaRepository<Plato, Long> {
}
