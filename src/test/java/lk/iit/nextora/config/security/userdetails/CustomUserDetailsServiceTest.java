package lk.iit.nextora.config.security.userdetails;

import lk.iit.nextora.common.enums.UserRole;
import lk.iit.nextora.common.enums.UserStatus;
import lk.iit.nextora.module.auth.entity.Admin;
import lk.iit.nextora.module.auth.entity.BaseUser;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.auth.repository.BaseUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @SuppressWarnings("unchecked")
    private final BaseUserRepository<Student> studentRepo = mock(BaseUserRepository.class);
    @SuppressWarnings("unchecked")
    private final BaseUserRepository<Admin> adminRepo = mock(BaseUserRepository.class);

    private CustomUserDetailsService userDetailsService;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(List.of(studentRepo, adminRepo));

        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setEmail("student@iit.ac.lk");
        testStudent.setPassword("encoded-password");
        testStudent.setFirstName("John");
        testStudent.setLastName("Doe");
        testStudent.setRole(UserRole.ROLE_STUDENT);
        testStudent.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should find user from first repository that has a match")
    void loadUserByUsername_userInFirstRepo_returnsUser() {
        // Given
        when(studentRepo.findByEmail("student@iit.ac.lk")).thenReturn(Optional.of(testStudent));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("student@iit.ac.lk");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("student@iit.ac.lk");
    }

    @Test
    @DisplayName("Should search next repository when first returns empty")
    void loadUserByUsername_userInSecondRepo_returnsUser() {
        // Given
        Admin admin = new Admin();
        admin.setId(2L);
        admin.setEmail("admin@iit.ac.lk");
        admin.setPassword("encoded");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(UserRole.ROLE_ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        when(studentRepo.findByEmail("admin@iit.ac.lk")).thenReturn(Optional.empty());
        when(adminRepo.findByEmail("admin@iit.ac.lk")).thenReturn(Optional.of(admin));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("admin@iit.ac.lk");

        // Then
        assertThat(result.getUsername()).isEqualTo("admin@iit.ac.lk");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when no repository has user")
    void loadUserByUsername_userNotFound_throwsException() {
        // Given
        when(studentRepo.findByEmail("unknown@iit.ac.lk")).thenReturn(Optional.empty());
        when(adminRepo.findByEmail("unknown@iit.ac.lk")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@iit.ac.lk"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should return UserDetails that implements BaseUser interface correctly")
    void loadUserByUsername_existingUser_returnsCorrectAuthorities() {
        // Given
        when(studentRepo.findByEmail("student@iit.ac.lk")).thenReturn(Optional.of(testStudent));

        // When
        UserDetails result = userDetailsService.loadUserByUsername("student@iit.ac.lk");

        // Then
        assertThat(result.getAuthorities()).isNotEmpty();
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
    }
}
