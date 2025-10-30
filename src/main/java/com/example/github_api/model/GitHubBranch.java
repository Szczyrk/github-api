package com.example.github_api.model;

public record GitHubBranch(
    String name,
    Commit commit
) {
    public record Commit(String sha) {}
}
