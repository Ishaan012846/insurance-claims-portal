package com.example.claims;

import com.example.claims.user.Role;
import com.example.claims.user.User;
import com.example.claims.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and find user by email")
    void shouldSaveAndFindUserByEmail() {
        User user = User.builder()
                .email("john.doe@example.com")
                .passwordHash("hashed_secret")
                .fullName("John Doe")
                .role(Role.CUSTOMER)
                .build();

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<User> found = userRepository.findByEmail("john.doe@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
        assertThat(found.get().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        User user = User.builder()
                .email("jane.smith@example.com")
                .passwordHash("hashed_secret")
                .fullName("Jane Smith")
                .role(Role.HANDLER)
                .build();

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("jane.smith@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }
}
