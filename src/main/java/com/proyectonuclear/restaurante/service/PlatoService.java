package com.proyectonuclear.restaurante.service;

import com.proyectonuclear.restaurante.dto.PlatoRequest;
import com.proyectonuclear.restaurante.dto.PlatoResponse;
import com.proyectonuclear.restaurante.entity.Plato;
import com.proyectonuclear.restaurante.exception.RecursoNoEncontradoException;
import com.proyectonuclear.restaurante.exception.ReglaNegocioException;
import com.proyectonuclear.restaurante.repository.PedidoRepository;
import com.proyectonuclear.restaurante.repository.PlatoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlatoService {

    private final PlatoRepository platoRepository;
    private final PedidoRepository pedidoRepository;

    public PlatoService(PlatoRepository platoRepository, PedidoRepository pedidoRepository) {
        this.platoRepository = platoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public PlatoResponse crear(PlatoRequest request) {
        Plato plato = new Plato(
                request.nombre().strip(),
                limpiarDescripcion(request.descripcion()),
                request.precio(),
                request.disponible()
        );
        return convertir(platoRepository.save(plato));
    }

    @Transactional(readOnly = true)
    public List<PlatoResponse> listar() {
        return platoRepository.findAll(Sort.by("id")).stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatoResponse buscar(Long id) {
        return convertir(encontrarPlato(id));
    }

    @Transactional
    public PlatoResponse actualizar(Long id, PlatoRequest request) {
        Plato plato = encontrarPlato(id);
        plato.setNombre(request.nombre().strip());
        plato.setDescripcion(limpiarDescripcion(request.descripcion()));
        plato.setPrecio(request.precio());
        plato.setDisponible(request.disponible());
        return convertir(platoRepository.save(plato));
    }

    @Transactional
    public void eliminar(Long id) {
        Plato plato = encontrarPlato(id);
        if (pedidoRepository.contarPedidosQueIncluyenPlato(id) > 0) {
            throw new ReglaNegocioException(
                    "No se puede eliminar un plato asociado a pedidos. Puedes marcarlo como no disponible.");
        }
        platoRepository.delete(plato);
    }

    private Plato encontrarPlato(Long id) {
        return platoRepository.findById(id).orElseThrow(() ->
                new RecursoNoEncontradoException("No existe un plato con ID " + id));
    }

    private String limpiarDescripcion(String descripcion) {
        return descripcion == null ? null : descripcion.strip();
    }

    private PlatoResponse convertir(Plato plato) {
        return new PlatoResponse(plato.getId(), plato.getNombre(), plato.getDescripcion(),
                plato.getPrecio(), plato.isDisponible());
    }
}
