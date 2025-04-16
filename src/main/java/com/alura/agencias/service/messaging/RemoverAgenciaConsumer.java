package com.alura.agencias.service.messaging;

import com.alura.agencias.domain.messaging.AgenciaMessage;
import com.alura.agencias.repository.AgenciaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class RemoverAgenciaConsumer {

    private ObjectMapper objectMapper;
    private AgenciaRepository agenciaRepository;

    public RemoverAgenciaConsumer(ObjectMapper objectMapper, AgenciaRepository agenciaRepository) {
        this.objectMapper = objectMapper;
        this.agenciaRepository = agenciaRepository;
    }

    @WithTransaction
    @Incoming("banking-service-channel")
    public Uni<Void> consumirMensagem(String mensagem) {
        return Uni.createFrom().item(() -> {
                    try {
                        Log.info(mensagem);
                        return objectMapper.readValue(mensagem, AgenciaMessage.class);
                    } catch (JsonProcessingException e) {
                        Log.error("Erro ao deserializar mensagem Kafka", e);
                        throw new RuntimeException(e);
                    }
                }).onItem()
                .transformToUni(agenciaMessage ->
                        agenciaRepository.findByCnpj(agenciaMessage.getCnpj())
                                .onItem().ifNotNull().transformToUni(agencia ->
                                        agenciaRepository.deleteById(agencia.getId())
                                ).replaceWithVoid()
                );
    }
}