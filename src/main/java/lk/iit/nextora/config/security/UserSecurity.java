package lk.iit.nextora.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final SecurityService securityService;

    public boolean isOwner(Long userId) {
        Long currentUserId = securityService.getCurrentUserId();
        return userId.equals(currentUserId);
    }
}

