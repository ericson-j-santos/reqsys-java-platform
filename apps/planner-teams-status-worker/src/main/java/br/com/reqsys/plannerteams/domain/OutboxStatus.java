package br.com.reqsys.plannerteams.domain;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    SENT,
    ERROR,
    DEAD_LETTER
}
