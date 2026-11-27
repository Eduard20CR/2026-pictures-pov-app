package com.picturepov.api.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/** For @WebMvcTest: the real security rules, so controller tests see the same 401 and 403 as production. */
@TestConfiguration
@Import(SecurityConfiguration.class)
public class WebLayerTestConfiguration {
}
