package com.example.github_api.service;

import com.example.github_api.model.GitHubRepositoryResponse;
import com.example.github_api.model.GitHubRepositoryResponse.BranchInfo;
import com.example.github_api.model.GitHubBranch;
import com.example.github_api.model.GitHubRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);

    private final RestClient restClient;

    public GitHubService(RestClient.Builder builder, @Value("${github.api.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<GitHubRepositoryResponse> getUserRepositories(String username) {
        try {
            GitHubRepo[] repos = restClient.get()
                    .uri("/users/{username}/repos", username)
                    .retrieve()
                    .body(GitHubRepo[].class);

            if (repos == null) {
                throw new RuntimeException("GitHub API returned null for repos");
            }

            log.info("Fetched {} repos for user {}", repos.length, username);

            return List.of(repos).stream()
                    .filter(repo -> !repo.fork())
                    .map(repo -> {
                        GitHubBranch[] branches = restClient.get()
                                .uri("/repos/{username}/{repo}/branches", username, repo.name())
                                .retrieve()
                                .body(GitHubBranch[].class);

                        List<BranchInfo> branchList = branches == null ? List.of() :
                                List.of(branches).stream()
                                        .map(b -> new BranchInfo(b.name(), b.commit().sha()))
                                        .toList();

                        return new GitHubRepositoryResponse(repo.name(), repo.owner().login(), branchList);
                    })
                    .toList();

        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("User not found: " + username);
        } catch (HttpClientErrorException.Forbidden e) {
            if (e.getResponseBodyAsString().contains("rate limit")) {
                throw new RateLimitExceededException("GitHub API rate limit exceeded");
            }
            throw e;
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String msg) {
            super(msg);
        }
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String msg) {
            super(msg);
        }
    }
}
