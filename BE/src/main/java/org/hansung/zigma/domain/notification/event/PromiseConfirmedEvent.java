package org.hansung.zigma.domain.notification.event;

import java.time.LocalDateTime;

public record PromiseConfirmedEvent(
        Long promiseId,
        String promiseTitle,
        LocalDateTime promisedAt,
        Long candidateId,
        String candidateName,
        String candidateAddress
) {
}
