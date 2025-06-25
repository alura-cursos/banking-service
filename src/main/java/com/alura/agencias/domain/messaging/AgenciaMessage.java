package com.alura.agencias.domain.messaging;

public class AgenciaMessage {

    public AgenciaMessage() {

    }

    public AgenciaMessage(Long id, String nome, String razaoSocial, String cnpj, String situacaoCadastral) {
        this.id = id;
        this.nome = nome;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.situacaoCadastral = situacaoCadastral;
    }

    private Long id;
    private String nome;
    private String razaoSocial;
    private String cnpj;
    private String situacaoCadastral;

    public Long getId() {
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
