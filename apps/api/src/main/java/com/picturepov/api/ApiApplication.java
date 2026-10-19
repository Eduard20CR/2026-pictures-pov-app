package com.picturepov.api;

import com.picturepov.api.configuration.SsmDbSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SsmDbSettings.load();
        SpringApplication.run(ApiApplication.class, args);
    }

}
