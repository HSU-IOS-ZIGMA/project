package org.hansung.zigma.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.hansung.zigma.domain.promise.entity.Role;
import org.hansung.zigma.domain.promise.repository.PromiseMemberRepository;
import org.hansung.zigma.domain.user.entity.User;
import org.hansung.zigma.domain.user.exception.UserNotFoundException;
import org.hansung.zigma.domain.user.repository.UserRepository;
import org.hansung.zigma.domain.user.web.dto.UserRes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PromiseMemberRepository promiseMemberRepository;

    @Override
    public UserRes getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        long joinedPromiseCount = promiseMemberRepository.countByUserId(userId);
        long createdPromiseCount = promiseMemberRepository.countByUserIdAndRole(userId, Role.HOST);

        return UserRes.of(user, joinedPromiseCount, createdPromiseCount);
    }
}
