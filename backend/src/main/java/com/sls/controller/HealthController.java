package com.sls.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("application", "SLMS");
        try (Connection conn = dataSource.getConnection()) {
            result.put("database", conn.getCatalog());
            result.put("dbConnected", true);
        } catch (Exception e) {
            result.put("dbConnected", false);
            result.put("dbError", e.getMessage());
        }
        return result;
    }
}
