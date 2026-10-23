package com.proyectonuclear.restaurante.dto;

import com.proyectonuclear.restaurante.entity.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public record PedidoEstadoRequest(
        @NotNull(message = "El estado es obligatorio") EstadoPedido estado
) {
}
