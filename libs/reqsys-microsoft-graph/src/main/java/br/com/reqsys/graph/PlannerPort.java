package br.com.reqsys.graph;

public interface PlannerPort {
    String criarTarefa(String titulo, String descricao, String bucketId);
    PlannerTask consultarTarefa(String taskId);
    record PlannerTask(String id, String title, String percentComplete, String assignedToEmail) {}
}
