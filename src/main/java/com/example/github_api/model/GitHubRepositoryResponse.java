package com.example.github_api.model;

import java.util.List;

public record GitHubRepositoryResponse(
    String repositoryName,
    String ownerLogin,
    List<BranchInfo> branches
) {
    public record BranchInfo(String name, String lastCommitSha) {}
}