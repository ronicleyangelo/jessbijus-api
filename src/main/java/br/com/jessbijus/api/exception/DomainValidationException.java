package br.com.jessbijus.api.exception;

public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String message) { super(message); }
}
