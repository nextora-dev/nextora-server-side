package lk.iit.nextora.module.auth.repository;

import lk.iit.nextora.module.auth.entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseUserRepository<T extends BaseUser> extends JpaRepository<T, Long> {
    Optional<T> findByEmail(String email);
}
