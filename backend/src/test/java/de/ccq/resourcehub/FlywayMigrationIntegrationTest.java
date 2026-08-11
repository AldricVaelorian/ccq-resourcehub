package de.ccq.resourcehub;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class FlywayMigrationIntegrationTest {

    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @Test
    void migrate_appliesAllVersionedMigrationsToEmptyPostgresDatabase() throws SQLException {
        // arrange
        var flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load();

        // act
        var result = flyway.migrate();

        // assert
        assertThat(result.migrationsExecuted).isEqualTo(2);
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .containsExactly("1", "2");
        assertThat(publicTables()).containsExactlyInAnyOrder("block_times", "flyway_schema_history");
        assertThat(blockTimeIndexes())
                .contains("block_times_pkey", "idx_block_times_resource_id", "idx_block_times_date_range");
    }

    private List<String> publicTables() throws SQLException {
        var tables = new ArrayList<String>();
        try (var connection = DriverManager.getConnection(
                        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                var result = connection.getMetaData().getTables(null, "public", null, new String[] {"TABLE"})) {
            while (result.next()) {
                tables.add(result.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    private List<String> blockTimeIndexes() throws SQLException {
        var indexes = new ArrayList<String>();
        try (var connection = DriverManager.getConnection(
                        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                var result = connection.getMetaData().getIndexInfo(null, "public", "block_times", false, false)) {
            while (result.next()) {
                indexes.add(result.getString("INDEX_NAME"));
            }
        }
        return indexes;
    }
}
