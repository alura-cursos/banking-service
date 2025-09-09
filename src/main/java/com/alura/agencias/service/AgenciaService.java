package com.alura.agencias.service;

import com.alura.agencias.domain.Agencia;
import com.alura.agencias.domain.http.AgenciaHttp;
import com.alura.agencias.domain.http.SituacaoCadastral;
import com.alura.agencias.exception.AgenciaNaoAtivaOuNaoEncontradaException;
import com.alura.agencias.repository.AgenciaRepository;
import com.alura.agencias.service.cache.RedisCacheService;
import com.alura.agencias.service.http.SituacaoCadastralHttpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
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
    private final RedisCacheService redisCacheService;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    AgenciaService(AgenciaRepository agenciaRepository, MeterRegistry meterRegistry, RedisCacheService redisCacheService, Tracer tracer) {
        this.agenciaRepository = agenciaRepository;
        this.meterRegistry = meterRegistry;
        this.redisCacheService = redisCacheService;
        this.objectMapper = new ObjectMapper();
        this.tracer = tracer;
    }

    @RestClient
    SituacaoCadastralHttpService situacaoCadastralHttpService;

    @WithTransaction // to do -> usando o hibernate sem panache ainda precisaria manter a transação aberta com o @WithTransaction
    public Uni<Void> cadastrar(Agencia agencia) {
        Span span = tracer.spanBuilder("cadastrarAgencia").startSpan();
        span.setAttribute("agencia.cnpj", agencia.getCnpj());
        Counter counter = this.meterRegistry.counter("agencia_nao_adicionada_count");
        return situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj())
                .onItem()
                    .ifNull().failWith(new AgenciaNaoAtivaOuNaoEncontradaException())
                    .invoke(a -> Log.info("Agencia com CNPJ " + a.getCnpj() + " foi encontrada"))
                    .invoke(t -> counter.increment())
                    .invoke(a -> {
                        try(Scope scope = span.makeCurrent()) {
                            span.addEvent("Agencia encontrada");
                            span.setAttribute("agencia.encontrada", a.getCnpj());
                        }
                    }).eventually(() -> {
                        span.end();
                })
                .onItem().transformToUni(agenciaHttpTransformada -> persistirSeEstaAtiva(agenciaHttpTransformada, agencia, counter));
    }

    private Uni<Void> persistirSeEstaAtiva(AgenciaHttp agenciaHttp, Agencia agencia, Counter counter) {
        if(agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.ATIVO)) {
            return agenciaRepository.persist(agencia)
                    .invoke(t -> this.meterRegistry.counter("agencia_adicionada_count").increment()) // to do -> pesquisar se seria bloqueante e como resolver caso seja
                    .invoke(a -> Log.info("Agencia com CNPJ " + agencia.getCnpj() + " foi adicionada"))
                    .replaceWithVoid();
        } else {
            Log.error("Agencia com CNPJ " + agencia.getCnpj() + " não ativa"); // to do -> pesquisar aqui tb.
            counter.increment();
            return Uni.createFrom().failure(new AgenciaNaoAtivaOuNaoEncontradaException());
        }
    }

    @WithSession
    public Uni<Agencia> buscarPorId(Long id) {
        String key = "agencia_" + id;
        return buscarNoCache(key).onItem().ifNull().switchTo(buscarNoBanco(key, id));
    }

    public Uni<Agencia> buscarNoCache(String key) {
        this.meterRegistry.counter("agencia_busca_cache_count").increment();
        return redisCacheService.get(key)
                .onItem().ifNotNull().transform(agencia -> {
                    try {
                        Log.info("Agência encontrada no cache");
                        return objectMapper.readValue(agencia, Agencia.class);
                    } catch (Exception e) {
                        Log.error("Objeto não pode ser encontrado");
                        return null;
                    }
                });
    }

    public Uni<Agencia> buscarNoBanco(String key, Long id) {
        return agenciaRepository.findById(id)
                .onItem().ifNotNull().call(agencia -> {
                    try {
                        Log.info("Setando informação no cache");
                        return redisCacheService.set(key, objectMapper.writeValueAsString(agencia), 3600);
                    } catch (Exception e) {
                        return Uni.createFrom().failure(e);
                    }
                });
    }


    @WithTransaction
    public Uni<Void> deletar(Long id) {
        String key = "agencia_" + id;

        return agenciaRepository.deleteById(id)
                .call(a -> redisCacheService.del(key))
                .invoke(a -> Log.info("A agência foi deletada"))
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
                ).invoke(a -> Log.info("A agência com CNPJ " + agencia.getCnpj() + " foi alterada"))
                .replaceWithVoid();
    }
}
