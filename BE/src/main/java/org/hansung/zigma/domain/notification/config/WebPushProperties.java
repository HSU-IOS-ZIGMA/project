package org.hansung.zigma.domain.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "web-push")
public class WebPushProperties {

    private String vapidPublicKey;
    private String vapidPrivateKey;
    private String vapidSubject;
    private int ttlSeconds = 3600;

    public boolean isEnabled() {
        return hasText(vapidPublicKey) && hasText(vapidPrivateKey) && hasText(vapidSubject);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
