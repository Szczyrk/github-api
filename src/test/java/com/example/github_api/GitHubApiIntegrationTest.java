package com.example.github_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GitHubApiIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate restTemplate = new RestTemplate();
	@SuppressWarnings("unchecked")
    @Test
    void shouldReturnNonForkRepositoriesWithBranches() {
        // given
        String user = "octocat";

        // when
        ResponseEntity<List> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/github/" + user + "/repositories",
            HttpMethod.GET,
            null,
            List.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> repos = response.getBody();
        assertThat(repos).isNotEmpty();

        Map firstRepo = (Map) repos.get(0);
        assertThat(firstRepo).containsKeys("repositoryName", "ownerLogin", "branches");

        List<?> branches = (List<?>) firstRepo.get("branches");
        assertThat(branches).isNotEmpty();

        Map firstBranch = (Map) branches.get(0);
        assertThat(firstBranch).containsKeys("name", "lastCommitSha");
    }
}
