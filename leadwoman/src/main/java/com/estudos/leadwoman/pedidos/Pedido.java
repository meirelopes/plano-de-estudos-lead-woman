package com.estudos.leadwoman.pedidos;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Pedido {
    private String pedidoId;
    private String cliente;
    private String produto;
    private Integer quantidade;
}
