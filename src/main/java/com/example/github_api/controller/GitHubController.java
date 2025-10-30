package com.example.github_api.controller;

import com.example.github_api.model.GitHubRepositoryResponse;
import com.example.github_api.service.GitHubService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/{username}/repositories")
    public List<GitHubRepositoryResponse> getRepositories(@PathVariable String username) {
        return gitHubService.getUserRepositories(username);
    }
}
