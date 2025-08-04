package com.example.github_api.controller;

import com.example.github_api.model.GitHubRepositoryResponse;
import com.example.github_api.service.GitHubService;
import com.example.github_api.service.GitHubService.NoRepositoriesFoundException;
import com.example.github_api.service.GitHubService.RateLimitExceededException;
import com.example.github_api.service.GitHubService.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/{username}/repositories")
    public ResponseEntity<?> getRepositories(@PathVariable String username) {
        try {
            List<GitHubRepositoryResponse> repos = gitHubService.getUserRepositories(username);
            return ResponseEntity.ok(repos);
        } catch (UserNotFoundException | NoRepositoriesFoundException e) {
            return ResponseEntity.status(404).body(
                Map.of(
                    "status", 404,
                    "message", e.getMessage()
                )
            );
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(429).body(
                Map.of(
                    "status", 429,
                    "message", e.getMessage()
                )
            );
        }
    }
}