package com.estudos.leadwoman.pedidos;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PedidoController {

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @RequestMapping("/pedidos")
    public ResponseEntity<?> criarPedido(@Valid @RequestBody PedidoRequestDto dto) {

        logger.info("Recebendo pedido com ID: {}", dto.getPedidoId());

        pedidoService.enviarParaSNS(dto);

        return ResponseEntity.accepted().build();
    }
}
