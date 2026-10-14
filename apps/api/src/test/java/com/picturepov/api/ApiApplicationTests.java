package com.picturepov.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    void failsOnPurpose() {
        org.junit.jupiter.api.Assertions.fail("Testing that CI blocks the merge");
    }
}
