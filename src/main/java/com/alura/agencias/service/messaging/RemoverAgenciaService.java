package com.alura.agencias.service.messaging;

import br.com.alura.Agencia;
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
public class RemoverAgenciaService {

    private final ObjectMapper objectMapper;
    private final AgenciaRepository agenciaRepository;

    public RemoverAgenciaService(AgenciaRepository agenciaRepository) {
        this.agenciaRepository = agenciaRepository;
        objectMapper = new ObjectMapper();
    }

    @WithTransaction
    @Incoming("remover-agencia-channel")
    public Uni<Void> consumirMensagem(Agencia mensagem) {
        try {
            Log.info(mensagem);
            //AgenciaMessage agenciaMessage = objectMapper.readValue(mensagem, AgenciaMessage.class);
            AgenciaMessage agenciaMessage =
                    new AgenciaMessage(mensagem.getNome(),
                            mensagem.getRazaoSocial(),
                            mensagem.getCnpj(),
                            mensagem.getSituacaoCadastral());
            return agenciaRepository.findByCnpj(agenciaMessage.getCnpj())
                    .onItem().ifNotNull().transformToUni(agencia ->
                        agenciaRepository.deleteById(agencia.getId())).replaceWithVoid();
        } catch (Exception e) {
            Log.error(e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }
}
