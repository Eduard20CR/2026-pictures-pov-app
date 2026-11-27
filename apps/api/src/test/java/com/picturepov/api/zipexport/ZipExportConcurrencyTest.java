package com.picturepov.api.zipexport;

import com.picturepov.api.TestData;
import com.picturepov.api.invitation.InvitationRole;
import com.picturepov.api.invitation.InvitationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * The event row lock in ZipExportService.request: concurrent requests start one ZIP.
 * Not @Transactional, so each request commits on its own connection; rows are removed after each test.
 */
@SpringBootTest
class ZipExportConcurrencyTest {

    static final int REQUESTS = 5;

    @Autowired ZipExportService sut;
    @Autowired JdbcClient jdbc;
    @MockitoBean ZipExportQueue queue;

    UUID event;
    UUID organizer;

    @BeforeEach
    void setUp() {
        TestData data = new TestData(jdbc);
        event = data.event();
        organizer = data.user("ana-" + UUID.randomUUID() + "@example.com");
        data.invite(event, organizer, InvitationRole.OWNER, InvitationStatus.ACCEPTED);
        data.photo(event, "2099-01-01T10:00:00Z", "READY");
    }

    @AfterEach
    void tearDown() {
        jdbc.sql("DELETE FROM zip_exports WHERE event_id = :id").param("id", event).update();
        jdbc.sql("DELETE FROM images WHERE event_id = :id").param("id", event).update();
        jdbc.sql("DELETE FROM invitations WHERE event_id = :id").param("id", event).update();
        jdbc.sql("DELETE FROM events WHERE id = :id").param("id", event).update();
        jdbc.sql("DELETE FROM users WHERE id = :id").param("id", organizer).update();
    }

    @Test
    void concurrentRequestsStartASingleZip() throws Exception {
        // Arrange
        CountDownLatch start = new CountDownLatch(1);
        Callable<ZipExportResponse> request = () -> {
            start.await();
            return sut.request(event, organizer);
        };
        ExecutorService pool = Executors.newFixedThreadPool(REQUESTS);

        // Act
        List<Future<ZipExportResponse>> futures = IntStream.range(0, REQUESTS).mapToObj(i -> pool.submit(request)).toList();
        start.countDown();
        List<ZipExportResponse> responses = new ArrayList<>();
        for (Future<ZipExportResponse> future : futures) {
            responses.add(future.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        // Assert
        assertThat(responses).extracting(ZipExportResponse::status).containsOnly(ZipExportResponse.State.IN_PROGRESS);
        int zips = jdbc.sql("SELECT COUNT(*) FROM zip_exports WHERE event_id = :id").param("id", event).query(Integer.class).single();
        assertThat(zips).isEqualTo(1);
        verify(queue, times(1)).send(any(UUID.class));
    }
}
