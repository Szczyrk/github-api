package com.example.github_api;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.profiles.active=test"
)

@AutoConfigureWireMock(port = 8888)
public class GitHubApiIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setupMocks() {
        // 1. Mock /users/octocat/repos
        stubFor(get(urlEqualTo("/users/octocat/repos"))
                .willReturn(okJson("""
                    [
                      {
                        "name": "real-repo",
                        "fork": false,
                        "owner": { "login": "octocat" }
                      },
                      {
                        "name": "forked-repo",
                        "fork": true,
                        "owner": { "login": "octocat" }
                      }
                    ]
                """)));

        // 2. Mock /repos/octocat/real-repo/branches
        stubFor(get(urlEqualTo("/repos/octocat/real-repo/branches"))
                .willReturn(okJson("""
                    [
                      {
                        "name": "main",
                        "commit": {
                          "sha": "abc123"
                        }
                      }
                    ]
                """)));
    }

    @Test
    void shouldReturnOnlyNonForkRepositoriesWithBranches() {
        webTestClient.get()
                .uri("http://localhost:" + port + "/api/github/octocat/repositories")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("""
                    [
                      {
                        "repositoryName": "real-repo",
                        "ownerLogin": "octocat",
                        "branches": [
                          {
                            "name": "main",
                            "lastCommitSha": "abc123"
                          }
                        ]
                      }
                    ]
                """);
    }
}
