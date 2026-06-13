package org.hansung.zigma.domain.notification.web.dto;

import org.hansung.zigma.domain.notification.entity.NotificationType;

public record WebPushPayload(
        NotificationType type,
        String title,
        String body,
        String url,
        Long targetId
) {
}
