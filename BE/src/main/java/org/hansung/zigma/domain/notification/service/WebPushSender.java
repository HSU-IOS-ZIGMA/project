package org.hansung.zigma.domain.notification.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.hansung.zigma.domain.notification.config.WebPushProperties;
import org.hansung.zigma.domain.notification.entity.PushSubscription;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebPushSender {

    private final WebPushProperties properties;
    private PushService pushService;

    @PostConstruct
    public void init() {
        if (!properties.isEnabled()) {
            log.info("Web Push is disabled because VAPID properties are not configured.");
            return;
        }

        try {
            this.pushService = new PushService(
                    properties.getVapidPublicKey(),
                    properties.getVapidPrivateKey(),
                    properties.getVapidSubject()
            );
            log.info("Web Push PushService initialized.");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Web Push PushService.", e);
        }
    }

    public WebPushSendResult send(PushSubscription subscription, String payload) {
        if (pushService == null) {
            log.info("Web Push is disabled. subscriptionId={}", subscription.getId());
            return new WebPushSendResult(false, null);
        }

        try {
            nl.martijndwars.webpush.Notification notification = new nl.martijndwars.webpush.Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dh(),
                    subscription.getAuth(),
                    payload.getBytes(StandardCharsets.UTF_8),
                    properties.getTtlSeconds()
            );

            HttpResponse response = pushService.send(notification, Encoding.AES128GCM);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                log.info("Web Push sent. subscriptionId={}, statusCode={}", subscription.getId(), statusCode);
            } else {
                log.warn("Web Push failed with response status. subscriptionId={}, statusCode={}, responseBody={}",
                        subscription.getId(),
                        statusCode,
                        readResponseBody(response));
            }
            return new WebPushSendResult(statusCode >= 200 && statusCode < 300, statusCode);
        } catch (Exception e) {
            log.warn("Failed to send web push. subscriptionId={}", subscription.getId(), e);
            return new WebPushSendResult(false, null);
        }
    }

    private String readResponseBody(HttpResponse response) {
        if (response.getEntity() == null) {
            return "";
        }

        try {
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            return "Failed to read response body: " + e.getMessage();
        }
    }
}
