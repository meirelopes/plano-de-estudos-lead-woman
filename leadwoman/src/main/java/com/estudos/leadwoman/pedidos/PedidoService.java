package com.estudos.leadwoman.pedidos;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;

import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);
    private final AmazonSNS snsClient;
    private final String topicArn = "arn:aws:sns:us-east-1:000000000000:pedido-criado-topic";
    private final ObjectMapper objectMapper;

    public PedidoService() {

        this.snsClient = AmazonSNSClientBuilder.standard()
                .withEndpointConfiguration(
                        new AmazonSNSClientBuilder.EndpointConfiguration("http://localhost:4566", "us-east-1"))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public void enviarParaSNS(PedidoRequestDto dto) {

        try {
            String message = objectMapper.writeValueAsString(dto);
            logger.info("Enviando mensagem para SNS: {}", message);

            PublishRequest publishRequest = new PublishRequest(topicArn, message);
            PublishResult result = snsClient.publish(publishRequest);

            logger.info("Mensagem enviada. MessageId: {}", result.getMessageId());
        } catch (JsonProcessingException e) {
            logger.error("Erro ao converter DTO para JSON", e);
            throw new RuntimeException("Erro ao converter pedido para JSON", e);
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem para SNS", e);
            throw new RuntimeException("Erro ao enviar pedido para SNS", e);
        }
    }
}

