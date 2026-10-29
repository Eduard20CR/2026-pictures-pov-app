package com.picturepov.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
class AwsConfiguration {
    @Bean
    S3Presigner s3Presigner() {
        return S3Presigner.create();
    }
}
