package org.hansung.zigma.domain.user.service;

import org.hansung.zigma.domain.user.web.dto.UserRes;

public interface UserService {

    UserRes getMyProfile(Long userId);
}
