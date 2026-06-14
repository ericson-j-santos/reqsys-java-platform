package br.com.reqsys.redminecopilot.web.dto;

public record RedmineSyncResponse(
        String id,
        String status,
        String referenciaExterna
) {}
