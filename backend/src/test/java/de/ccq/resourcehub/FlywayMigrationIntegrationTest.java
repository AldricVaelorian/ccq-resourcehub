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
        assertThat(result.migrationsExecuted).isEqualTo(5);
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .containsExactly("1", "2", "3", "4", "5");
        assertThat(publicTables())
                .containsExactlyInAnyOrder(
                        "availability_rules",
                        "block_times",
                        "bookings",
                        "flyway_schema_history",
                        "resources",
                        "users");
        assertThat(blockTimeIndexes())
                .contains("block_times_pkey", "idx_block_times_resource_id", "idx_block_times_date_range");
        assertThat(availabilityRuleIndexes())
                .contains(
                        "availability_rules_pkey",
                        "idx_availability_rules_day_of_week",
                        "idx_availability_rules_active");
        assertThat(indexesFor("bookings"))
                .contains("bookings_pkey", "idx_bookings_resource_status_dates");
        assertThat(columnsFor("bookings")).contains("rejected_at", "rejection_reason");
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
        return indexesFor("block_times");
    }

    private List<String> availabilityRuleIndexes() throws SQLException {
        return indexesFor("availability_rules");
    }

    private List<String> indexesFor(String tableName) throws SQLException {
        var indexes = new ArrayList<String>();
        try (var connection = DriverManager.getConnection(
                        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                var result = connection.getMetaData().getIndexInfo(null, "public", tableName, false, false)) {
            while (result.next()) {
                indexes.add(result.getString("INDEX_NAME"));
            }
        }
        return indexes;
    }

    private List<String> columnsFor(String tableName) throws SQLException {
        var columns = new ArrayList<String>();
        try (var connection = DriverManager.getConnection(
                        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                var result = connection.getMetaData().getColumns(null, "public", tableName, null)) {
            while (result.next()) {
                columns.add(result.getString("COLUMN_NAME"));
            }
        }
        return columns;
    }
}
