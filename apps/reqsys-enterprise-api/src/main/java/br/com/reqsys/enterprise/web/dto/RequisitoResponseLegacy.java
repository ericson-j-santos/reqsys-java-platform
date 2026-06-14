package br.com.reqsys.enterprise.web.dto;

/** Mantida apenas para compatibilidade com ProcessarRequisitoUseCase (legado). */
@Deprecated
public record RequisitoResponseLegacy(
        String id,
        String titulo,
        String historiaUsuario,
        String criteriosBdd,
        String confianca
) {}
