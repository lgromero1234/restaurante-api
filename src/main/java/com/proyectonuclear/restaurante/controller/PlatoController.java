package com.proyectonuclear.restaurante.controller;

import com.proyectonuclear.restaurante.dto.PlatoRequest;
import com.proyectonuclear.restaurante.dto.PlatoResponse;
import com.proyectonuclear.restaurante.service.PlatoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/platos")
public class PlatoController {

    private final PlatoService platoService;

    public PlatoController(PlatoService platoService) {
        this.platoService = platoService;
    }

    @PostMapping
    public ResponseEntity<PlatoResponse> crear(@Valid @RequestBody PlatoRequest request) {
        PlatoResponse respuesta = platoService.crear(request);
        return ResponseEntity.created(URI.create("/api/platos/" + respuesta.id())).body(respuesta);
    }

    @GetMapping
    public List<PlatoResponse> listar() {
        return platoService.listar();
    }

    @GetMapping("/{id}")
    public PlatoResponse buscar(@PathVariable Long id) {
        return platoService.buscar(id);
    }

    @PutMapping("/{id}")
    public PlatoResponse actualizar(@PathVariable Long id, @Valid @RequestBody PlatoRequest request) {
        return platoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        platoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
