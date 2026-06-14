package br.com.reqsys.graph;

public interface TeamsPort {
    void enviarMensagemUsuario(String emailDestinatario, String mensagemMarkdown);
    boolean usuarioExiste(String email);
}
