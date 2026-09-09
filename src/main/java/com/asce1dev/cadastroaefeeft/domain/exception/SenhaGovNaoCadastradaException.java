package com.asce1dev.cadastroaefeeft.domain.exception;

public class SenhaGovNaoCadastradaException extends EntidadeNaoEncontradaException {

    private static final long serialVersionUID = 1L;

    public SenhaGovNaoCadastradaException(Long clienteId) {
        super(String.format("Cliente de código %d não possui senha Gov.br cadastrada", clienteId));
    }
}
