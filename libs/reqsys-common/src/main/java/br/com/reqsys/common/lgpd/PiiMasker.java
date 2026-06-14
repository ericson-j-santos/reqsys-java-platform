package br.com.reqsys.common.lgpd;

public final class PiiMasker {
    private PiiMasker() {}

    public static String email(String valor) {
        if (valor == null || !valor.contains("@")) return "***";
        String[] partes = valor.split("@", 2);
        String nome = partes[0];
        String dominio = partes[1];
        String prefixo = nome.length() <= 2 ? "**" : nome.substring(0, 2) + "***";
        return prefixo + "@" + dominio;
    }

    public static String cpf(String valor) {
        if (valor == null) return "***";
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() != 11) return "***";
        return "***.***.***-" + digitos.substring(9);
    }

    public static String telefone(String valor) {
        if (valor == null) return "***";
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() < 4) return "***";
        return "***" + digitos.substring(digitos.length() - 4);
    }
}
