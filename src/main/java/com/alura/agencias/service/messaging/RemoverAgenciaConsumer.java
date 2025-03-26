package com.alura.agencias.service.messaging;

import com.alura.agencias.domain.Agencia;
import com.alura.agencias.domain.messaging.AgenciaMessage;
import com.alura.agencias.repository.AgenciaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Incoming("banking-service-channel")
    public Uni<Void> consumirMensagem(String mensagem) {
        Log.info(mensagem);
        try {
            AgenciaMessage agenciaMessage = objectMapper.readValue(mensagem, AgenciaMessage.class);
            Uni<Agencia> agenciaRecuperada = agenciaRepository.findByCnpj(agenciaMessage.getCnpj());
            return agenciaRecuperada.onItem().ifNotNull().transform(agencia -> agenciaRepository.deleteById(agencia.getId())).replaceWithVoid();
        } catch (JsonProcessingException e) {
            Log.error(e.getMessage());
            throw new RuntimeException();
        }
    }
}
