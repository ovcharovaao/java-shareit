package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Проверка существования пользователя по email (игнорируя регистр)")
    void existsByEmailIgnoreCase_shouldReturnTrueWhenEmailExists() {
        User user = new User();
        user.setName("User1");
        user.setEmail("user1@example.com");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmailIgnoreCase("USER1@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Проверка существования пользователя по email (игнорируя регистр), когда email не существует")
    void existsByEmailIgnoreCase_shouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmailIgnoreCase("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Проверка сохранения и поиска пользователя по id")
    void findById_shouldReturnUserWhenIdExists() {
        User user = new User();
        user.setName("User1");
        user.setEmail("user1@example.com");
        userRepository.save(user);

        User foundUser = userRepository.findById(user.getId()).orElse(null);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo(user.getName());
        assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
    }
}
