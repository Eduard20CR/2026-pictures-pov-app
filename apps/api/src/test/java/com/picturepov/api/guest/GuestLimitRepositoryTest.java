package com.picturepov.api.guest;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class GuestLimitRepositoryTest {

    @Autowired GuestLimitRepository guestLimitRepository;
    @Autowired JdbcClient jdbc;

    UUID eventId = UUID.randomUUID();
    UUID guestId = UUID.randomUUID();

    @BeforeEach
    void createEvent() {
        jdbc.sql("""
                INSERT INTO events (id, name, color, public_code, event_date, images_per_user, status)
                VALUES (:id, 'Test event', '#000000', :code, DATE '2026-12-12', 2, 'ACTIVE')
                """)
                .param("id", eventId)
                .param("code", eventId.toString().substring(0, 22))
                .update();
    }

    @Test
    void stopsReservingWhenLimitIsReached(){
        boolean first = guestLimitRepository.reserveUpload(eventId, guestId, 2);
        boolean second = guestLimitRepository.reserveUpload(eventId, guestId, 2);
        boolean third = guestLimitRepository.reserveUpload(eventId, guestId, 2);

        assertThat(first).isTrue();
        assertThat(second).isTrue();
        assertThat(third).isFalse();
        assertThat(guestLimitRepository.countUploads(eventId, guestId)).isEqualTo(2);
    }

}
