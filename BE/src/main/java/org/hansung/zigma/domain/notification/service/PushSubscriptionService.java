package org.hansung.zigma.domain.notification.service;

import org.hansung.zigma.domain.notification.web.dto.PushSubscriptionReq;

public interface PushSubscriptionService {

    void subscribe(Long userId, PushSubscriptionReq req, String userAgent);

    void unsubscribe(Long userId, PushSubscriptionReq req);
}
