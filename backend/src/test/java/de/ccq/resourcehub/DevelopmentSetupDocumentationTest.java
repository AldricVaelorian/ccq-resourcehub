package de.ccq.resourcehub;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DevelopmentSetupDocumentationTest {

    @ParameterizedTest
    @MethodSource("setupGuides")
    void localPostgresSetup_createsApplicationRoleBeforeDatabaseOwnedByRole(Path setupGuide) throws IOException {
        // arrange
        var documentation = Files.readString(setupGuide);

        // act & assert
        assertThat(documentation).containsSubsequence(
                "CREATE USER resourcehub_user WITH PASSWORD 'resourcehub_pass';",
                "CREATE DATABASE resourcehub OWNER resourcehub_user;");
    }

    private static Stream<Path> setupGuides() {
        return Stream.of(Path.of("..", "README.md"), Path.of("README.md"));
    }
}
