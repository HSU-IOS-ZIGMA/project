package org.hansung.zigma.domain.promise.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class CursorUtil {
    private static final String DELIMITER = "|";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 인코딩
    public static String encodeCursor(Long id, LocalDateTime promisedAt) {
        if (id == null || promisedAt == null) return null;
        String rawCursor = id + DELIMITER + promisedAt.format(FORMATTER);
        return Base64.getEncoder().encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    // 디코딩: Base64 -> [id, promisedAt]
    public static CursorContents decodeCursor(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isBlank()) return null;

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedCursor);
            String rawCursor = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = rawCursor.split("\\" + DELIMITER);

            Long id = Long.parseLong(parts[0]);
            LocalDateTime promisedAt = LocalDateTime.parse(parts[1], FORMATTER);

            return new CursorContents(id, promisedAt);
        } catch (Exception e) {
            return null;
        }
    }

    public record CursorContents(Long id, LocalDateTime promisedAt) {}
}
