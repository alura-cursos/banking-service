package com.alura.agencias.domain.http;

public class AgenciaHttp {

    private String nome;
    private String razaoSocial;
    private String cnpj;
    private SituacaoCadastral situacaoCadastral;

    public String getNome() {
        return nome;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public SituacaoCadastral getSituacaoCadastral() {
        return situacaoCadastral;
    }
}

