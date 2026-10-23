package com.proyectonuclear.restaurante;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectonuclear.restaurante.dto.PlatoRequest;
import com.proyectonuclear.restaurante.dto.PlatoResponse;
import com.proyectonuclear.restaurante.repository.PedidoRepository;
import com.proyectonuclear.restaurante.repository.PlatoRepository;
import com.proyectonuclear.restaurante.service.PlatoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestauranteApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private PlatoService platoService;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private PlatoRepository platoRepository;

    @BeforeEach
    void limpiar() {
        pedidoRepository.deleteAll();
        platoRepository.deleteAll();
    }

    @Test
    void crearPedidoConDosPlatosCalculaTotalYMantieneRelacion() throws Exception {
        PlatoResponse hamburguesa = crearPlato("Hamburguesa", "18000.00");
        PlatoResponse papas = crearPlato("Papas", "12000.00");

        String json = """
            {"nombreCliente":"Carlos Perez","platoIds":[%d,%d]}
            """.formatted(hamburguesa.id(), papas.id());

        String respuesta = mvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreCliente").value("Carlos Perez"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.platos.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        JsonNode body = mapper.readTree(respuesta);
        assertEquals(0, new BigDecimal("30000.00").compareTo(body.get("total").decimalValue()));

        mvc.perform(get("/api/pedidos/{id}", body.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platos.length()").value(2));
    }

    @Test
    void actualizarPrecioDePlatoNoModificaTotalDePedidoYaCreado() throws Exception {
        PlatoResponse plato = crearPlato("Hamburguesa", "18000.00");
        String pedidoJson = """
            {"nombreCliente":"Sara","platoIds":[%d]}
            """.formatted(plato.id());

        String pedidoCreado = mvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON).content(pedidoJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long pedidoId = mapper.readTree(pedidoCreado).get("id").asLong();

        String actualizarPlato = """
            {"nombre":"Hamburguesa","descripcion":"Especial","precio":29000,"disponible":true}
            """;
        mvc.perform(put("/api/platos/{id}", plato.id())
                        .contentType(MediaType.APPLICATION_JSON).content(actualizarPlato))
                .andExpect(status().isOk());

        String pedidoActualizado = mvc.perform(get("/api/pedidos/{id}", pedidoId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals(0, new BigDecimal("18000.00")
                .compareTo(mapper.readTree(pedidoActualizado).get("total").decimalValue()));
    }

    @Test
    void rechazaPlatosRepetidosYNoPermiteEliminarPlatosUsados() throws Exception {
        PlatoResponse plato = crearPlato("Hamburguesa", "18000.00");
        String repetido = """
            {"nombreCliente":"Laura","platoIds":[%d,%d]}
            """.formatted(plato.id(), plato.id());
        mvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON).content(repetido))
                .andExpect(status().isConflict());

        String valido = """
            {"nombreCliente":"Laura","platoIds":[%d]}
            """.formatted(plato.id());
        mvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON).content(valido))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/platos/{id}", plato.id()))
                .andExpect(status().isConflict());
    }

    @Test
    void rechazaDatosInvalidosYEstadosIlegales() throws Exception {
        mvc.perform(post("/api/platos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nombre":"","precio":-2,"disponible":true}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.nombre").exists())
                .andExpect(jsonPath("$.detalles.precio").exists());

        PlatoResponse plato = crearPlato("Agua", "5000.00");
        String json = """
            {"nombreCliente":"Luis","platoIds":[%d]}
            """.formatted(plato.id());
        String creado = mvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(creado).get("id").asLong();

        mvc.perform(patch("/api/pedidos/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ENTREGADO\"}"))
                .andExpect(status().isConflict());

        mvc.perform(patch("/api/pedidos/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PREPARACION\"}"))
                .andExpect(status().isOk());
    }

    private PlatoResponse crearPlato(String nombre, String precio) {
        return platoService.crear(new PlatoRequest(nombre, "Descripcion de prueba",
                new BigDecimal(precio), true));
    }
}
