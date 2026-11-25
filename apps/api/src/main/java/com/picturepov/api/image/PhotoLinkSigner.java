package com.picturepov.api.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

/** The one place that issues signed, temporary GET links to stored photo files (RN-08, RNF-SEG-05); each caller passes its own validity. Public for the ZIP link (#79). */
@Component
public class PhotoLinkSigner {

    private final S3Presigner s3Presigner;
    private final String bucket;

    PhotoLinkSigner(S3Presigner s3Presigner, @Value("${app.photos.bucket}") String bucket) {
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    /** A link the browser displays inline. */
    public String viewLink(String key, Duration ttl) {
        return sign(key, ttl, null);
    }

    /** A link the browser saves as fileName. */
    public String downloadLink(String key, String fileName, Duration ttl) {
        return sign(key, ttl, "attachment; filename=\"" + fileName + "\"");
    }

    private String sign(String key, Duration ttl, String contentDisposition) {
        return s3Presigner.presignGetObject(presign -> presign
                        .signatureDuration(ttl)
                        .getObjectRequest(get -> get.bucket(bucket).key(key)
                                .responseContentDisposition(contentDisposition)))
                .url().toString();
    }
}
