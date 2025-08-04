package com.example.github_api.service;

import com.example.github_api.model.GitHubRepositoryResponse;
import com.example.github_api.model.GitHubRepositoryResponse.BranchInfo;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {

    private final RestClient restClient = RestClient.create();

    public List<GitHubRepositoryResponse> getUserRepositories(String username) {
        URI url = URI.create("https://api.github.com/users/" + username + "/repos");

        try {
            var repos = restClient.get()
                .uri(url)
                .retrieve()
                .body(Map[].class);

            List<GitHubRepositoryResponse> responseList = new ArrayList<>();

            for (Map<String, Object> repo : repos) {
                if (repo == null || Boolean.TRUE.equals(repo.get("fork"))) {
                    continue;
                }

                String repoName = (String) repo.get("name");
                Map<String, Object> owner = (Map<String, Object>) repo.get("owner");
                if (repoName == null || owner == null) continue;

                String ownerLogin = (String) owner.get("login");
                if (ownerLogin == null) continue;

                URI branchesUrl = URI.create("https://api.github.com/repos/" + username + "/" + repoName + "/branches");
                var branches = restClient.get()
                    .uri(branchesUrl)
                    .retrieve()
                    .body(Map[].class);

                List<BranchInfo> branchList = new ArrayList<>();
                if (branches != null) {
                    for (Map<String, Object> branch : branches) {
                        if (branch == null) continue;

                        String branchName = (String) branch.get("name");
                        Map<String, Object> commit = (Map<String, Object>) branch.get("commit");

                        if (branchName == null || commit == null) continue;

                        String sha = (String) commit.get("sha");
                        if (sha == null) continue;

                        branchList.add(new BranchInfo(branchName, sha));
                    }
                }

                responseList.add(new GitHubRepositoryResponse(repoName, ownerLogin, branchList));
            }

            if (responseList.isEmpty()) {
                throw new NoRepositoriesFoundException("No public non-fork repositories found for user: " + username);
            }

            return responseList;
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("User not found: " + username);
        } catch (HttpClientErrorException.Forbidden e) {
            throw new RateLimitExceededException("GitHub API rate limit exceeded");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected error", e);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String msg) {
            super(msg);
        }
    }

    public static class NoRepositoriesFoundException extends RuntimeException 
    {
        public NoRepositoriesFoundException(String msg) {
            super(msg);
        }
    }

        public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String msg) {
            super(msg);
        }
    }
}
