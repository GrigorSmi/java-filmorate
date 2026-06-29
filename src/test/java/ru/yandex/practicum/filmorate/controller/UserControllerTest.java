package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    // создание пользователя с name = null → name становится равен login
    @Test
    void create_shouldUseLoginWhenNameIsNull() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("mylogin");
        user.setName(null);
        user.setBirthday(Instant.now());

        User result = controller.create(user);
        assertEquals("mylogin", result.getName());
    }

    // создание пользователя с name = "   " → name становится равен login
    @Test
    void create_shouldUseLoginWhenNameIsBlank() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("mylogin");
        user.setName("   ");
        user.setBirthday(Instant.now());

        User result = controller.create(user);
        assertEquals("mylogin", result.getName());
    }

    // создание пользователя с датой рождения = текущий момент → успех (граничное значение)
    @Test
    void create_shouldSucceedWhenBirthdayIsNow() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(Instant.now());

        User result = controller.create(user);
        assertNotNull(result.getId());
    }

    // создание пользователя с датой рождения в прошлом → успех
    @Test
    void create_shouldSucceedWhenBirthdayIsInPast() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(Instant.parse("2000-01-01T00:00:00Z"));

        User result = controller.create(user);
        assertNotNull(result.getId());
    }

    // создание пользователя с birthday = null → успех (поле необязательное)
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

    // создание полностью валидного пользователя → успех, присваивается id
    @Test
    void create_shouldSucceedWithValidUser() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("display name");
        user.setBirthday(Instant.parse("1990-06-15T00:00:00Z"));

        User result = controller.create(user);
        assertNotNull(result.getId());
        assertEquals("user@mail.com", result.getEmail());
        assertEquals("display name", result.getName());
    }

    // последовательное создание двух пользователей → id увеличиваются: 1, 2
    @Test
    void create_shouldGenerateIncrementingIds() {
        User first = new User();
        first.setEmail("a@mail.com");
        first.setLogin("a");
        first.setName("A");
        first.setBirthday(Instant.now());

        User second = new User();
        second.setEmail("b@mail.com");
        second.setLogin("b");
        second.setName("B");
        second.setBirthday(Instant.now());

        User r1 = controller.create(first);
        User r2 = controller.create(second);

        assertEquals(1L, r1.getId());
        assertEquals(2L, r2.getId());
    }

    // обновление пользователя без id → возвращается null
    @Test
    void update_shouldReturnNullWhenIdIsNull() {
        User update = new User();
        update.setId(null);
        update.setEmail("user@mail.com");
        update.setLogin("login");

        assertNull(controller.update(update));
    }

    // обновление несуществующего пользователя (id=999) → возвращается null
    @Test
    void update_shouldReturnNullWhenUserNotFound() {
        User update = new User();
        update.setId(999L);
        update.setEmail("user@mail.com");
        update.setLogin("login");
        update.setName("name");
        update.setBirthday(Instant.now());

        assertNull(controller.update(update));
    }

    // обновление пользователя с name = "   " → name становится равен login
    @Test
    void update_shouldUseLoginWhenNameIsBlank() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("original");
        user.setBirthday(Instant.now());
        controller.create(user);

        User update = new User();
        update.setId(1L);
        update.setEmail("user@mail.com");
        update.setLogin("login");
        update.setName("   ");
        update.setBirthday(Instant.now());

        User result = controller.update(update);
        assertEquals("login", result.getName());
    }

    // обновление существующего пользователя валидными данными → все поля меняются
    @Test
    void update_shouldSucceedWithValidData() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("login");
        user.setName("original");
        user.setBirthday(Instant.now());
        User created = controller.create(user);

        User update = new User();
        update.setId(created.getId());
        update.setEmail("newemail@mail.com");
        update.setLogin("newlogin");
        update.setName("new name");
        update.setBirthday(Instant.parse("1990-01-01T00:00:00Z"));

        User result = controller.update(update);
        assertEquals("newemail@mail.com", result.getEmail());
        assertEquals("newlogin", result.getLogin());
        assertEquals("new name", result.getName());
    }
}
