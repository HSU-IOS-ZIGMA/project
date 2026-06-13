package org.hansung.zigma.domain.notification.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PushSubscriptionReq {

    @NotBlank
    private String endpoint;

    @Valid
    @NotNull
    private Keys keys;

    @Getter
    @NoArgsConstructor
    public static class Keys {

        @NotBlank
        private String p256dh;

        @NotBlank
        private String auth;
    }
}
