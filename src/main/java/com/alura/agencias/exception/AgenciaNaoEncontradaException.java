package com.alura.agencias.exception;

public class AgenciaNaoEncontradaException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Agência não encontrada";
    }
}
