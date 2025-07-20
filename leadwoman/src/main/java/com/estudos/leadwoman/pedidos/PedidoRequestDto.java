package com.estudos.leadwoman.pedidos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PedidoRequestDto {
    @NotBlank(message = "Id não pode ser nulo ou vazio.")
    @JsonProperty("id") private String pedidoId;

    @NotBlank(message = "Cliente não pode ser nulo ou vazio.")
    @JsonProperty("cliente") private String cliente;

    @NotBlank(message = "Produto não pode ser nulo ou vazio.")
    @JsonProperty("produto") private String produto;

    @NotNull(message = "Quantidade não pode ser nula.")
    @JsonProperty("quantidade") private Integer quantidade;

    public String getPedidoId() {
        return pedidoId;
    }
}
