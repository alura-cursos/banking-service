package com.alura.agencias.utils;

import com.alura.agencias.domain.Agencia;
import com.alura.agencias.domain.Endereco;
import com.alura.agencias.domain.http.AgenciaHttp;

public class AgenciaFixture {

    public static AgenciaHttp criarAgenciaHttp(String status) {
        return new AgenciaHttp("Agencia Teste", "Razao social da Agencia Teste", "123", status);
    }

    public static Agencia criarAgencia() {
        Endereco endereco = new Endereco(1, "Rua de teste", "Logradouro de teste", "Complemento de teste", 1);
        return new Agencia(1, "Agencia Teste", "Razao social da Agencia Teste", "123", endereco);
    }
}
