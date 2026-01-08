package com.alura.agencias.service;

import br.com.alura.Agencia;
import com.alura.agencias.domain.messaging.AgenciaMessage;
import com.alura.agencias.repository.AgenciaRepository;
import com.alura.agencias.service.http.SagaHttpService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class RemoverAgenciaService {

    private final AgenciaRepository agenciaRepository;

    @RestClient
    SagaHttpService sagaHttpService;

    public RemoverAgenciaService(AgenciaRepository agenciaRepository) {
        this.agenciaRepository = agenciaRepository;
    }

    @WithTransaction
    @Incoming("remover-agencia-channel")
    public Uni<Void> consumirMensagem(Agencia mensagem) {
        try {
            AgenciaMessage agenciaMessage =
                    new AgenciaMessage(mensagem.getNome(),
                            mensagem.getRazaoSocial(),
                            mensagem.getCnpj(),
                            mensagem.getSituacaoCadastral());
            return agenciaRepository.findByCnpj(agenciaMessage.getCnpj())
                    .onItem().ifNotNull().transformToUni(agencia ->
                            agenciaRepository.deleteById(agencia.getId())
                                    .call(() -> sagaHttpService.fecharSaga(agencia.getCnpj()))
                    ).replaceWithVoid();
        } catch (Exception e) {
            return Uni.createFrom().failure(e);
        }
    }
}
