package com.alura.agencias.service;

import com.alura.agencias.domain.Agencia;
import com.alura.agencias.domain.http.AgenciaHttp;
import com.alura.agencias.domain.http.SituacaoCadastral;
import com.alura.agencias.exception.AgenciaNaoAtivaOuNaoEncontradaException;
import com.alura.agencias.repository.AgenciaRepository;
import com.alura.agencias.service.http.SituacaoCadastralHttpService;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AgenciaService {

    private final AgenciaRepository agenciaRepository;
    private final LongCounter counter;

    AgenciaService(AgenciaRepository agenciaRepository, Meter meter) {
        this.agenciaRepository = agenciaRepository;
        this.counter = meter.counterBuilder("agencia_nao_cadastrada")
                .setDescription("Quantidade de erros ao cadastrar agencia")
                .setUnit("invocations")
                .build();
    }

    @RestClient
    SituacaoCadastralHttpService situacaoCadastralHttpService;

    public void cadastrar(Agencia agencia) {
        AgenciaHttp agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());
        if(agenciaHttp != null && agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.ATIVO)) {
            Log.info("Agencia com CNPJ " + agencia.getCnpj() + " foi adicionada");
            agenciaRepository.persist(agencia);
        } else {
            counter.add(1);
            Log.info("Agencia com CNPJ " + agencia.getCnpj() + " não ativa ou não encontrada");
            throw new AgenciaNaoAtivaOuNaoEncontradaException();
        }
    }

    public Agencia buscarPorId(Long id) {
        return agenciaRepository.findById(id);
    }

    public void deletar(Long id) {
        Log.info("A agência foi deletada");
        agenciaRepository.deleteById(id);
    }

    public void alterar(Agencia agencia) {
        Log.info("A agência com CNPJ " + agencia.getCnpj() + " foi alterada");
        agenciaRepository.update("nome = ?1, razaoSocial = ?2, cnpj = ?3 where id = ?4", agencia.getNome(), agencia.getRazaoSocial(), agencia.getCnpj(), agencia.getId());
    }
}
