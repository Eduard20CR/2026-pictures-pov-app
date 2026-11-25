package com.picturepov.api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
class AwsConfiguration {
    @Bean
    S3Presigner s3Presigner(@Value("${app.aws.region}") String region) {
        return S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }

    /** Sends ZIP requests to the worker's queue (#294). */
    @Bean
    SqsClient sqsClient(@Value("${app.aws.region}") String region) {
        return SqsClient.builder()
                .region(Region.of(region))
                .build();
    }
}
