package com.alura.agencias.domain.messaging;

public class AgenciaMessage {

    public AgenciaMessage(Integer id, String nome, String razaoSocial, String cnpj, String situacaoCadastral) {
        this.id = id;
        this.nome = nome;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.situacaoCadastral = situacaoCadastral;
    }

    private final Integer id;
    private final String nome;
    private final String razaoSocial;
    private final String cnpj;
    private final String situacaoCadastral;

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getSituacaoCadastral() {
        return situacaoCadastral;
    }
}
