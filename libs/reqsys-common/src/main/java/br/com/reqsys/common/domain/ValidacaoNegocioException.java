package br.com.reqsys.common.domain;

public class ValidacaoNegocioException extends RuntimeException {
    private final String codigo;

    public ValidacaoNegocioException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
