package org.hansung.zigma.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.notification.entity.PushSubscription;
import org.hansung.zigma.domain.notification.repository.PushSubscriptionRepository;
import org.hansung.zigma.domain.notification.web.dto.PushSubscriptionReq;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.domain.user.exception.UserNotFoundException;
import org.hansung.zigma.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushSubscriptionServiceImpl implements PushSubscriptionService {

    private final UserRepository userRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Override
    @Transactional
    public void subscribe(Long userId, PushSubscriptionReq req, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String endpointHash = hashEndpoint(req.getEndpoint());

        pushSubscriptionRepository.findByEndpointHash(endpointHash)
                .ifPresentOrElse(
                        subscription -> subscription.renew(
                                user,
                                req.getKeys().getP256dh(),
                                req.getKeys().getAuth(),
                                userAgent
                        ),
                        () -> pushSubscriptionRepository.save(PushSubscription.create(
                                user,
                                req.getEndpoint(),
                                endpointHash,
                                req.getKeys().getP256dh(),
                                req.getKeys().getAuth(),
                                userAgent
                        ))
                );
    }

    @Override
    @Transactional
    public void unsubscribe(Long userId, PushSubscriptionReq req) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String endpointHash = hashEndpoint(req.getEndpoint());

        pushSubscriptionRepository.findByEndpointHash(endpointHash)
                .filter(subscription -> subscription.getUser().getId().equals(userId))
                .ifPresent(subscription -> subscription.expire(LocalDateTime.now()));
    }

    private String hashEndpoint(String endpoint) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(endpoint.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }
}
