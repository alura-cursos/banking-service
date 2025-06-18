package com.alura.agencias.service;

import com.alura.agencias.domain.Agencia;
import com.alura.agencias.domain.http.AgenciaHttp;
import com.alura.agencias.domain.http.SituacaoCadastral;
import com.alura.agencias.exception.AgenciaNaoAtivaOuNaoEncontradaException;
import com.alura.agencias.repository.AgenciaRepository;
import com.alura.agencias.service.http.SituacaoCadastralHttpService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AgenciaService {

    private final AgenciaRepository agenciaRepository;
    private final MeterRegistry meterRegistry;

    AgenciaService(AgenciaRepository agenciaRepository, MeterRegistry meterRegistry) {
        this.agenciaRepository = agenciaRepository;
        this.meterRegistry = meterRegistry;
    }

    @RestClient
    SituacaoCadastralHttpService situacaoCadastralHttpService;

    @WithTransaction // to do -> usando o hibernate sem panache ainda precisaria manter a transação aberta com o @WithTransaction
    public Uni<Void> cadastrar(Agencia agencia) {
        Counter counter = this.meterRegistry.counter("agencia_nao_adicionada_count");
        return situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj())
                .onItem().ifNull().failWith(new AgenciaNaoAtivaOuNaoEncontradaException()).log("Agencia com CNPJ " + agencia.getCnpj() + " foi encontrada").invoke(t -> counter.increment())
                .onItem().transformToUni(agenciaHttpTransformada -> persistirSeEstaAtiva(agenciaHttpTransformada, agencia, counter));
    }

    private Uni<Void> persistirSeEstaAtiva(AgenciaHttp agenciaHttp, Agencia agencia, Counter counter) {
        if(agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.ATIVO)) {
            return agenciaRepository.persist(agencia)
                    .invoke(t -> this.meterRegistry.counter("agencia_adicionada_count").increment()) // to do -> pesquisar se seria bloqueante e como resolver caso seja
                    .log("Agencia com CNPJ " + agencia.getCnpj() + " foi adicionada")
                    .replaceWithVoid();
        } else {
            Log.info("Agencia com CNPJ " + agencia.getCnpj() + " não ativa"); // to do -> pesquisar aqui tb.
            counter.increment();
            return Uni.createFrom().failure(new AgenciaNaoAtivaOuNaoEncontradaException());
        }
    }

    @WithSession
    public Uni<Agencia> buscarPorId(Long id) {
        return agenciaRepository.findById(id);
    }

    @WithTransaction
    public Uni<Void> deletar(Long id) {
        return agenciaRepository.deleteById(id)
                .log("A agência foi deletada") // to do -> verificar como poderia usar log level
                .replaceWithVoid();
    }

    @WithTransaction
    public Uni<Void> alterar(Agencia agencia) {
        return agenciaRepository
                .update("nome = ?1, razaoSocial = ?2, cnpj = ?3 where id = ?4",
                        agencia.getNome(),
                        agencia.getRazaoSocial(),
                        agencia.getCnpj(),
                        agencia.getId()
                ).log("A agência com CNPJ " + agencia.getCnpj() + " foi alterada")
                .replaceWithVoid();
    }
}
