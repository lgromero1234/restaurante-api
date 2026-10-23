package com.proyectonuclear.restaurante.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record RespuestaError(
        LocalDateTime fechaHora,
        int estado,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> detalles
) {
}
