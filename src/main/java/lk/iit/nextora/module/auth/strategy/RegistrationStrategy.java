package lk.iit.nextora.module.auth.strategy;

import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.dto.request.RegisterRequest;

public interface RegistrationStrategy {
    void validate(RegisterRequest request);
    BaseUser mapToEntity(RegisterRequest request);
    void postRegistration(BaseUser user);
}
