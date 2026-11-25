package com.picturepov.api.zipexport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.UUID;

/** The worker reads {"exportId":"<uuid>"} (apps/zip-export/src/handler.ts). */
@Component
class SqsZipExportQueue implements ZipExportQueue {

    private final SqsClient sqsClient;
    private final String queueUrl;

    SqsZipExportQueue(SqsClient sqsClient, @Value("${app.zips.queue-url}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void send(UUID exportId) {
        if (queueUrl.isBlank()) {
            throw new IllegalStateException("ZIP_EXPORT_QUEUE_URL is not set");
        }
        sqsClient.sendMessage(message -> message
                .queueUrl(queueUrl)
                .messageBody("{\"exportId\":\"" + exportId + "\"}"));
    }
}
