package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {
    @Autowired
    private UserController controller;

    @Autowired
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage.clearAll();
    }

    @Test
    void create_shouldUseLoginWhenNameIsNull() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("mylogin");
        user.setName(null);
        user.setBirthday(LocalDate.now());

        User result = controller.create(user);
        assertEquals("mylogin", result.getName());
    }

    @Test
    void create_shouldUseLoginWhenNameIsBlank() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("mylogin");
        user.setName("   ");
        user.setBirthday(LocalDate.now());

        User result = controller.create(user);
        assertEquals("mylogin", result.getName());
    }

    @Test
    void create_shouldSucceedWhenBirthdayIsNow() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.now());

        User result = controller.create(user);
        assertNotNull(result.getId());
    }

    @Test
    void create_shouldSucceedWhenBirthdayIsInPast() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User result = controller.create(user);
        assertNotNull(result.getId());
    }

    @Test
    void create_shouldSucceedWhenBirthdayIsNull() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(null);

        User result = controller.create(user);
        assertNotNull(result.getId());
    }

    @Test
    void create_shouldSucceedWithValidUser() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("display name");
        user.setBirthday(LocalDate.of(1990, 6, 15));

        User result = controller.create(user);
        assertNotNull(result.getId());
        assertEquals("user@mail.com", result.getEmail());
        assertEquals("display name", result.getName());
    }

    @Test
    void create_shouldGenerateIncrementingIds() {
        User first = new User();
        first.setEmail("a@mail.com");
        first.setLogin("a");
        first.setName("A");
        first.setBirthday(LocalDate.now());

        User second = new User();
        second.setEmail("b@mail.com");
        second.setLogin("b");
        second.setName("B");
        second.setBirthday(LocalDate.now());

        User r1 = controller.create(first);
        User r2 = controller.create(second);

        assertEquals(1L, r1.getId());
        assertEquals(2L, r2.getId());
    }

    @Test
    void delete_shouldRemoveUser() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.now());
        User created = controller.create(user);

        userStorage.delete(created.getId());

        assertTrue(userStorage.findById(created.getId()).isEmpty());
    }

    @Test
    void update_shouldThrowWhenIdIsNull() {
        User update = new User();
        update.setId(null);
        update.setEmail("user@mail.com");
        update.setLogin("login");

        assertThrows(ValidationException.class, () -> controller.update(update));
    }

    // обновление несуществующего пользователя (id=999) → ошибка 404
    @Test
    void update_shouldThrowWhenUserNotFound() {
        User update = new User();
        update.setId(999L);
        update.setEmail("user@mail.com");
        update.setLogin("login");
        update.setName("name");
        update.setBirthday(LocalDate.now());

        assertThrows(NotFoundException.class, () -> controller.update(update));
    }

    @Test
    void update_shouldUseLoginWhenNameIsBlank() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("original");
        user.setBirthday(LocalDate.now());
        controller.create(user);

        User update = new User();
        update.setId(1L);
        update.setEmail("user@mail.com");
        update.setLogin("login");
        update.setName("   ");
        update.setBirthday(LocalDate.now());

        User result = controller.update(update);
        assertEquals("login", result.getName());
    }

    @Test
    void update_shouldSucceedWithValidData() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("original");
        user.setBirthday(LocalDate.now());
        User created = controller.create(user);

        User update = new User();
        update.setId(created.getId());
        update.setEmail("newemail@mail.com");
        update.setLogin("newlogin");
        update.setName("new name");
        update.setBirthday(LocalDate.of(1990, 1, 1));

        User result = controller.update(update);
        assertEquals("newemail@mail.com", result.getEmail());
        assertEquals("newlogin", result.getLogin());
        assertEquals("new name", result.getName());
    }
}
