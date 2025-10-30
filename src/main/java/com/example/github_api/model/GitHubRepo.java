package com.example.github_api.model;

public record GitHubRepo(
    String name,
    boolean fork,
    Owner owner
) {
    public record Owner(String login) {}
}
