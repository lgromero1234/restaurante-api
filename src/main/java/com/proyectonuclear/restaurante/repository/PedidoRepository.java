package com.proyectonuclear.restaurante.repository;

import com.proyectonuclear.restaurante.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT COUNT(p) FROM Pedido p JOIN p.platos plato WHERE plato.id = :platoId")
    long contarPedidosQueIncluyenPlato(@Param("platoId") Long platoId);
}
