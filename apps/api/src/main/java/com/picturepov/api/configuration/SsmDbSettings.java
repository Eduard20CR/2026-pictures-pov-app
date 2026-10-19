package com.picturepov.api.configuration;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.Parameter;

public final class SsmDbSettings {

    private SsmDbSettings() {
    }

    public static void load() {
        String path = System.getenv("SSM_DB_PATH");

        if (path == null) return;

        try (SsmClient ssm = SsmClient.create()) {
            var response = ssm.getParametersByPath(r -> r.path(path).withDecryption(true));
            for (Parameter p : response.parameters()) {
                String property = p.name().substring(path.length() + 1);
                System.setProperty(property, p.value());
            }
        }
    }
}