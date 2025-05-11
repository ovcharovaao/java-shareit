package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(Long userId, BookingRequestDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> approve(Long userId, Long bookingId, Boolean approved) {
        Map<String, Object> params = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", userId, params, null);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, String state, int from, int size) {
        Map<String, Object> params = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> listByOwner(Long userId, String state) {
        Map<String, Object> params = Map.of("state", state);
        return get("/owner?state={state}", userId, params);
    }
}
