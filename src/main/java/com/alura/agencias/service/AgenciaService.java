package com.alura.agencias.service;

import com.alura.agencias.domain.Agencia;
import com.alura.agencias.domain.http.AgenciaHttp;
import com.alura.agencias.exception.AgenciaNaoAtivaException;
import com.alura.agencias.domain.http.SituacaoCadastral;
import com.alura.agencias.exception.AgenciaNaoEncontradaException;
import com.alura.agencias.service.http.SituacaoCadastralHttpService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AgenciaService {

    @RestClient
    SituacaoCadastralHttpService situacaoCadastralHttpService;

    private final List<Agencia> agencias = new ArrayList<>();

    public void cadastrar(Agencia agencia) {
        try {
            AgenciaHttp agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());
            if (agenciaHttp.getSituacaoCadastral() == SituacaoCadastral.ATIVO) {
                agencias.add(agencia);
            } else {
                throw new AgenciaNaoAtivaException();
            }
        } catch (AgenciaNaoAtivaException | AgenciaNaoEncontradaException e) {
            throw e;
        }
    }

    public Agencia buscarPorId(Long id) {
        return agencias.stream().filter(agencia -> agencia.getId().equals(id)).toList().get(0);
    }

    public void deletar(Long id) {
        agencias.removeIf(agencia -> agencia.getId().equals(id));
    }

    public Agencia alterar(Agencia agencia) {
        deletar(agencia.getId());
        agencias.add(agencia);
        return agencia;
    }
}
