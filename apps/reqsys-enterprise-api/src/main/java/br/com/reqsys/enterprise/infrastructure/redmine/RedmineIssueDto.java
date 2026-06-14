package br.com.reqsys.enterprise.infrastructure.redmine;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RedmineIssueDto(
        @JsonProperty("project_id") String projectId,
        @JsonProperty("tracker_id") Integer trackerId,
        @JsonProperty("subject")    String subject,
        @JsonProperty("description") String description,
        @JsonProperty("parent_issue_id") Integer parentIssueId
) {
    public RedmineIssueDto(String projectId, Integer trackerId, String subject, String description) {
        this(projectId, trackerId, subject, description, null);
    }
}
