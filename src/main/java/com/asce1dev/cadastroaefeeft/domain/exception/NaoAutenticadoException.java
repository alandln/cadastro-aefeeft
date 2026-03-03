package com.asce1dev.cadastroaefeeft.domain.exception;

public class NaoAutenticadoException extends RuntimeException {
    public NaoAutenticadoException(String message) {
        super(message);
    }
}
