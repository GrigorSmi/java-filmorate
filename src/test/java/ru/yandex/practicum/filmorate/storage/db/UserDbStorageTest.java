package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional; // <-- 1. ДОБАВЛЕНО: для изоляции тестов
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@Transactional // <-- 2. ДОБАВЛЕНО: автоматически откатывает изменения в БД после каждого теста
@RequiredArgsConstructor
class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbc;

    private User createUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setName("name_" + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void testAdd() {
        User user = createUser("login1", "user1@mail.com");
        User saved = userStorage.add(user);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testFindById() {
        User user = createUser("login2", "user2@mail.com");
        User saved = userStorage.add(user);

        Optional<User> found = userStorage.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getLogin()).isEqualTo("login2");
        assertThat(found.get().getEmail()).isEqualTo("user2@mail.com");
        assertThat(found.get().getName()).isEqualTo("name_login2");
        assertThat(found.get().getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testFindByIdNotFound() {
        Optional<User> found = userStorage.findById(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll() {
        // Очищаем перед проверкой размера, чтобы тест был стабильным
        jdbc.update("DELETE FROM users");
        userStorage.add(createUser("login_a", "a@mail.com"));
        userStorage.add(createUser("login_b", "b@mail.com"));

        assertThat(userStorage.findAll()).hasSize(2);
    }

    @Test
    void testUpdate() {
        User user = createUser("login3", "user3@mail.com");
        User saved = userStorage.add(user);

        saved.setEmail("updated@mail.com");
        saved.setLogin("updated_login");
        saved.setName("updated_name");
        User updated = userStorage.update(saved);

        assertThat(updated.getEmail()).isEqualTo("updated@mail.com");
        assertThat(updated.getLogin()).isEqualTo("updated_login");
        assertThat(updated.getName()).isEqualTo("updated_name");

        Optional<User> found = userStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("updated@mail.com");
    }

    @Test
    void testDelete() {
        User user = createUser("login4", "user4@mail.com");
        User saved = userStorage.add(user);

        userStorage.delete(saved.getId());

        assertThat(userStorage.findById(saved.getId())).isEmpty();
    }

    @Test
    void testClearAll() {
        userStorage.add(createUser("login_x", "x@mail.com"));
        userStorage.add(createUser("login_y", "y@mail.com"));

        userStorage.clearAll();

        assertThat(userStorage.findAll()).isEmpty();
    }

    @Test
    void testAddUserWithNullName() {
        User user = createUser("login5", "user5@mail.com");
        user.setName(null);
        User saved = userStorage.add(user);

        Optional<User> found = userStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isNull();
    }
}