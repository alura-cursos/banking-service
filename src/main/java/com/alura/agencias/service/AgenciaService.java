package com.alura.agencias.service;

import com.alura.agencias.domain.Agencia;
import com.alura.agencias.domain.http.AgenciaHttp;
import com.alura.agencias.domain.http.SituacaoCadastral;
import com.alura.agencias.exception.AgenciaNaoAtivaOuNaoEncontradaException;
import com.alura.agencias.repository.AgenciaRepository;
import com.alura.agencias.service.http.SituacaoCadastralHttpService;
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

    @WithTransaction
    public Uni<Void> cadastrar(Agencia agencia) {
        Uni<AgenciaHttp> agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());
        return agenciaHttp
                .onItem().ifNull().failWith(new AgenciaNaoAtivaOuNaoEncontradaException())
                .onItem().transformToUni(item -> persistirSeEstaAtiva(agencia, item));
    }

    private Uni<Void> persistirSeEstaAtiva(Agencia agencia,AgenciaHttp agenciaHttp) {
        if(agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.ATIVO)) {
            this.meterRegistry.counter("agencia_adicionada_count").increment();
            Log.info("Agencia com CNPJ " + agencia.getCnpj() + " foi adicionada");
            return agenciaRepository.persist(agencia).replaceWithVoid();
        } else {
            Log.info("Agencia com CNPJ " + agencia.getCnpj() + " não ativa ou não encontrada");
            this.meterRegistry.counter("agencia_nao_adicionada_count").increment();
            return Uni.createFrom().failure(new AgenciaNaoAtivaOuNaoEncontradaException()).replaceWithVoid();
        }
    }

    @WithSession
    public Uni<Agencia> buscarPorId(Long id) {
        return agenciaRepository.findById(id);
    }

    @WithTransaction
    public Uni<Void> deletar(Long id) {
        Log.info("A agência foi deletada");
        return agenciaRepository.deleteById(id).replaceWithVoid();
    }

    @WithTransaction
    public Uni<Void> alterar(Agencia agencia) {
        Log.info("A agência com CNPJ " + agencia.getCnpj() + " foi alterada");
        return agenciaRepository.update("nome = ?1, razaoSocial = ?2, cnpj = ?3 where id = ?4", agencia.getNome(), agencia.getRazaoSocial(), agencia.getCnpj(), agencia.getId()).replaceWithVoid();
    }
}
