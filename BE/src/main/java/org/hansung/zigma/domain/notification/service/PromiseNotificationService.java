package org.hansung.zigma.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hansung.zigma.domain.notification.entity.Notification;
import org.hansung.zigma.domain.notification.entity.NotificationTargetType;
import org.hansung.zigma.domain.notification.entity.NotificationType;
import org.hansung.zigma.domain.notification.entity.PushSubscription;
import org.hansung.zigma.domain.notification.event.PromiseConfirmedEvent;
import org.hansung.zigma.domain.notification.repository.NotificationRepository;
import org.hansung.zigma.domain.notification.repository.PushSubscriptionRepository;
import org.hansung.zigma.domain.notification.web.dto.WebPushPayload;
import org.hansung.zigma.domain.promise.entity.PromiseMember;
import org.hansung.zigma.domain.promise.repository.PromiseMemberRepository;
import org.hansung.zigma.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromiseNotificationService {

    private static final String PROMISE_DETAIL_URL_FORMAT = "/promises/%d";

    private final PromiseMemberRepository promiseMemberRepository;
    private final NotificationRepository notificationRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final WebPushSender webPushSender;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendPromiseConfirmedNotification(PromiseConfirmedEvent event) {
        List<User> members = promiseMemberRepository.findAllByPromiseId(event.promiseId()).stream()
                .map(PromiseMember::getUser)
                .toList();

        if (members.isEmpty()) {
            return;
        }

        List<Long> userIds = members.stream()
                .map(User::getId)
                .toList();

        Map<Long, List<PushSubscription>> subscriptionsByUserId = pushSubscriptionRepository
                .findAllByUserIdInAndIsActiveTrue(userIds)
                .stream()
                .collect(Collectors.groupingBy(subscription -> subscription.getUser().getId()));
        int subscriptionCount = subscriptionsByUserId.values().stream()
                .mapToInt(List::size)
                .sum();

        log.info("Start promise confirmed notification. promiseId={}, memberCount={}, subscriptionCount={}",
                event.promiseId(),
                members.size(),
                subscriptionCount);

        String title = "약속 장소가 확정됐어요";
        String body = "%s - %s".formatted(event.promiseTitle(), event.candidateName());
        String linkUrl = PROMISE_DETAIL_URL_FORMAT.formatted(event.promiseId());
        String payload = toPayload(event, title, body, linkUrl);
        LocalDateTime now = LocalDateTime.now();

        for (User member : members) {
            Notification notification = Notification.create(
                    member,
                    NotificationType.PROMISE_CONFIRMED,
                    title,
                    body,
                    NotificationTargetType.PROMISE,
                    event.promiseId(),
                    linkUrl
            );

            boolean sent = sendToUserSubscriptions(
                    subscriptionsByUserId.getOrDefault(member.getId(), List.of()),
                    payload,
                    now
            );

            if (sent) {
                notification.markSent(now);
            }

            notificationRepository.save(notification);
        }

        log.info("Finished promise confirmed notification. promiseId={}", event.promiseId());
    }

    private boolean sendToUserSubscriptions(List<PushSubscription> subscriptions, String payload, LocalDateTime now) {
        boolean sent = false;

        for (PushSubscription subscription : subscriptions) {
            WebPushSendResult result = webPushSender.send(subscription, payload);

            if (result.success()) {
                subscription.markUsed(now);
                sent = true;
            }

            if (result.isExpiredSubscription()) {
                subscription.expire(now);
            }
        }

        return sent;
    }

    private String toPayload(PromiseConfirmedEvent event, String title, String body, String linkUrl) {
        try {
            return objectMapper.writeValueAsString(new WebPushPayload(
                    NotificationType.PROMISE_CONFIRMED,
                    title,
                    body,
                    linkUrl,
                    event.promiseId()
            ));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize promise confirmed push payload. promiseId={}", event.promiseId(), e);
            return "{\"title\":\"%s\",\"body\":\"%s\",\"url\":\"%s\"}".formatted(title, body, linkUrl);
        }
    }
}
