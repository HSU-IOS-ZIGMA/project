package org.hansung.zigma.domain.notification.service;

public record WebPushSendResult(
        boolean success,
        Integer statusCode
) {

    public boolean isExpiredSubscription() {
        return statusCode != null && (statusCode == 404 || statusCode == 410);
    }
}
