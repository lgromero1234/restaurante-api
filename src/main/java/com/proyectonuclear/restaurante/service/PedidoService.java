package com.proyectonuclear.restaurante.service;

import com.proyectonuclear.restaurante.dto.PedidoRequest;
import com.proyectonuclear.restaurante.dto.PedidoResponse;
import com.proyectonuclear.restaurante.dto.PlatoResumenResponse;
import com.proyectonuclear.restaurante.entity.EstadoPedido;
import com.proyectonuclear.restaurante.entity.Pedido;
import com.proyectonuclear.restaurante.entity.Plato;
import com.proyectonuclear.restaurante.exception.RecursoNoEncontradoException;
import com.proyectonuclear.restaurante.exception.ReglaNegocioException;
import com.proyectonuclear.restaurante.repository.PedidoRepository;
import com.proyectonuclear.restaurante.repository.PlatoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Bogota");
    private final PedidoRepository pedidoRepository;
    private final PlatoRepository platoRepository;

    public PedidoService(PedidoRepository pedidoRepository, PlatoRepository platoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.platoRepository = platoRepository;
    }

    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        Set<Plato> platos = obtenerPlatos(request.platoIds(), Set.of());
        Pedido pedido = new Pedido(
                request.nombreCliente().strip(),
                LocalDateTime.now(ZONA_HORARIA),
                EstadoPedido.PENDIENTE,
                calcularTotal(platos),
                platos
        );
        return convertir(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAll(Sort.by("id")).stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id) {
        return convertir(encontrarPedido(id));
    }

    @Transactional
    public PedidoResponse actualizar(Long id, PedidoRequest request) {
        Pedido pedido = encontrarPedido(id);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede editar un pedido en estado PENDIENTE");
        }

        Set<Long> idsActuales = pedido.getPlatos().stream()
                .map(Plato::getId)
                .collect(Collectors.toSet());
        Set<Long> nuevosIds = new HashSet<>(request.platoIds());

        pedido.setNombreCliente(request.nombreCliente().strip());
        if (!idsActuales.equals(nuevosIds)) {
            // Los platos existentes pueden permanecer aunque luego se hayan deshabilitado.
            Set<Plato> platos = obtenerPlatos(request.platoIds(), idsActuales);
            pedido.setPlatos(platos);
            // El total solo se recalcula cuando cambia el contenido del pedido.
            pedido.setTotal(calcularTotal(platos));
        }
        return convertir(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido estadoNuevo) {
        Pedido pedido = encontrarPedido(id);
        EstadoPedido actual = pedido.getEstado();
        if (actual == estadoNuevo) {
            return convertir(pedido);
        }

        boolean transicionValida = switch (actual) {
            case PENDIENTE -> estadoNuevo == EstadoPedido.EN_PREPARACION
                    || estadoNuevo == EstadoPedido.CANCELADO;
            case EN_PREPARACION -> estadoNuevo == EstadoPedido.ENTREGADO
                    || estadoNuevo == EstadoPedido.CANCELADO;
            case ENTREGADO, CANCELADO -> false;
        };
        if (!transicionValida) {
            throw new ReglaNegocioException(
                    "No se puede cambiar el estado de " + actual + " a " + estadoNuevo);
        }
        pedido.setEstado(estadoNuevo);
        return convertir(pedidoRepository.save(pedido));
    }

    @Transactional
    public void eliminar(Long id) {
        Pedido pedido = encontrarPedido(id);
        pedidoRepository.delete(pedido);
    }

    private Pedido encontrarPedido(Long id) {
        return pedidoRepository.findById(id).orElseThrow(() ->
                new RecursoNoEncontradoException("No existe un pedido con ID " + id));
    }

    private Set<Plato> obtenerPlatos(List<Long> ids, Set<Long> idsYaAsociados) {
        Set<Long> idsUnicos = new LinkedHashSet<>(ids);
        if (ids.size() != idsUnicos.size()) {
            throw new ReglaNegocioException("Un mismo plato no puede repetirse dentro del pedido");
        }

        Map<Long, Plato> encontrados = new HashMap<>();
        platoRepository.findAllById(idsUnicos).forEach(plato -> encontrados.put(plato.getId(), plato));
        Set<Plato> platos = new LinkedHashSet<>();

        for (Long id : idsUnicos) {
            Plato plato = encontrados.get(id);
            if (plato == null) {
                throw new RecursoNoEncontradoException("No existe un plato con ID " + id);
            }
            if (!plato.isDisponible() && !idsYaAsociados.contains(id)) {
                throw new ReglaNegocioException("El plato con ID " + id + " no esta disponible");
            }
            platos.add(plato);
        }
        return platos;
    }

    private BigDecimal calcularTotal(Set<Plato> platos) {
        return platos.stream()
                .map(Plato::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2);
    }

    private PedidoResponse convertir(Pedido pedido) {
        List<PlatoResumenResponse> platos = pedido.getPlatos().stream()
                .sorted(Comparator.comparing(Plato::getId))
                .map(plato -> new PlatoResumenResponse(plato.getId(), plato.getNombre()))
                .toList();
        return new PedidoResponse(pedido.getId(), pedido.getNombreCliente(), pedido.getFechaHora(),
                pedido.getEstado(), pedido.getTotal(), platos);
    }
}
