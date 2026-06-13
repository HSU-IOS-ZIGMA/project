package org.hansung.zigma.domain.notification.repository;

import org.hansung.zigma.domain.notification.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    Optional<PushSubscription> findByEndpointHash(String endpointHash);

    List<PushSubscription> findAllByUserIdInAndIsActiveTrue(Collection<Long> userIds);
}
