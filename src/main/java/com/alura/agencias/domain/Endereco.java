package com.alura.agencias.domain;

class Endereco {

    Endereco(Integer id, String rua, String logradouro, String complemento, Integer numero) {
        this.id = id;
        this.rua = rua;
        this.logradouro = logradouro;
        this.complemento = complemento;
        this.numero = numero;
    }

    private final Integer id;
    private final String rua;
    private final String logradouro;
    private final String complemento;
    private final Integer numero;

    public Integer getId() {
        return id;
    }

    public String getRua() {
        return rua;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getComplemento() {
        return complemento;
    }

    public Integer getNumero() {
        return numero;
    }
}
