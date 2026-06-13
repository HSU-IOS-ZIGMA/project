package org.hansung.zigma.domain.presence.web.dto;

import java.time.OffsetDateTime;

public record PresencePingRes(
        String type,
        Long promiseId,
        String message,
        OffsetDateTime sentAt
) {
    public static PresencePingRes from(Long promiseId, PresencePingReq request) {
        return new PresencePingRes(
                "PRESENCE_PING",
                promiseId,
                request.message(),
                OffsetDateTime.now()
        );
    }
}
