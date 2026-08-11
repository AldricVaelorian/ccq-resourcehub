package de.ccq.resourcehub;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DockerComposeConfigurationTest {

    private static final Path DOCKER_COMPOSE_FILE = Path.of("..", "docker-compose.yml");

    @Test
    void postgresService_usesEnvironmentDrivenConnectionSettings() throws IOException {
        // arrange
        var composeConfiguration = Files.readString(DOCKER_COMPOSE_FILE);

        // act & assert
        assertThat(composeConfiguration)
                .contains("POSTGRES_DB: ${POSTGRES_DB:-resourcehub}")
                .contains("POSTGRES_USER: ${POSTGRES_USER:-resourcehub_user}")
                .contains("POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-resourcehub_pass}")
                .contains("\"${POSTGRES_PORT:-5432}:5432\"")
                .contains("pg_isready -U ${POSTGRES_USER:-resourcehub_user} "
                        + "-d ${POSTGRES_DB:-resourcehub}");
    }

    @Test
    void postgresService_mountsNamedVolumeAtPostgres18DataRoot() throws IOException {
        // arrange
        var composeConfiguration = Files.readString(DOCKER_COMPOSE_FILE);

        // act & assert
        assertThat(composeConfiguration)
                .contains("image: postgres:18-alpine")
                .contains("postgres_data:/var/lib/postgresql")
                .doesNotContain("postgres_data:/var/lib/postgresql/data");
    }
}
