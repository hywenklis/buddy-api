package com.buddy.api.integrations.web.account.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.integrations.IntegrationTestAbstract;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdoptionStatusHistorySchemaTest extends IntegrationTestAbstract {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Should have column 'adoption_status' in table after migration")
    void should_have_adoption_status_column_after_migration() throws Exception {
        try (final var connection = dataSource.getConnection();
             final var resultSet = connection.getMetaData()
                 .getColumns(null, null, "adoption_status_history", "adoption_status")) {

            assertThat(resultSet.next())
                .as("Column 'adoption_status' should exist in table 'adoption_status_history'")
                .isTrue();
        }
    }

    @Test
    @DisplayName("Should NOT have column 'status_name' in table after migration")
    void should_not_have_status_name_column_after_migration() throws Exception {
        try (final var connection = dataSource.getConnection();
             final var resultSet = connection.getMetaData()
                 .getColumns(null, null, "adoption_status_history", "status_name")) {

            assertThat(resultSet.next())
                .as("Column 'status_name' should NOT exist after renaming to 'adoption_status'")
                .isFalse();
        }
    }
}
