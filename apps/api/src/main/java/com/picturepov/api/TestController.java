package com.picturepov.api;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
class TestController {

    private final JdbcClient jdbc;

    public TestController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/db-check")
    public String dbCheck(){
        Integer one = jdbc.sql("SELECT 1").query(Integer.class).single();
        return one == 1 ? "ok": "error";
    }


}
