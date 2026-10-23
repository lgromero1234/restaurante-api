package com.proyectonuclear.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoRequest(
        @NotBlank(message = "El nombre del cliente es obligatorio")
        @Size(max = 120, message = "El nombre del cliente no puede superar 120 caracteres")
        String nombreCliente,

        @NotEmpty(message = "El pedido debe tener al menos un plato")
        List<@NotNull(message = "El ID no puede ser nulo")
             @Positive(message = "El ID debe ser positivo") Long> platoIds
) {
}
