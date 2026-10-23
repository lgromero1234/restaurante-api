package com.proyectonuclear.restaurante.dto;

import com.proyectonuclear.restaurante.entity.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String nombreCliente,
        LocalDateTime fechaHora,
        EstadoPedido estado,
        BigDecimal total,
        List<PlatoResumenResponse> platos
) {
}
