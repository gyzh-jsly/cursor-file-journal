package com.filejournal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void selectCountFromWatchedFolder() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM watched_folder",
                Long.class);
        System.out.println("SELECT COUNT(*) FROM watched_folder => " + count);
        assertNotNull(count);
    }
}
