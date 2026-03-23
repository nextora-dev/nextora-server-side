package lk.iit.nextora.config.security.userdetails;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.repository.BaseUserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // Inject all concrete BaseUserRepository beans and search through them to avoid autowire ambiguity
    private final List<BaseUserRepository<? extends BaseUser>> userRepositories;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        for (BaseUserRepository<? extends BaseUser> repo : userRepositories) {
            Optional<? extends BaseUser> opt = repo.findByEmail(email);
            if (opt.isPresent()) {
                return opt.get();
            }
        }
        throw new UsernameNotFoundException("User not found: " + email);
    }
}
