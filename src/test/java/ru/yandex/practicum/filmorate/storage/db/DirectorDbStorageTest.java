package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(DirectorDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DirectorDbStorageTest {
    private final DirectorDbStorage directorStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM film_directors");
        jdbc.update("DELETE FROM directors");
    }

    @Test
    void testCreate() {
        Director director = new Director();
        director.setName("Тарантино");
        Director saved = directorStorage.create(director);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Тарантино");
    }

    @Test
    void testGetById() {
        Director director = new Director();
        director.setName("Нолан");
        Director saved = directorStorage.create(director);
        Optional<Director> found = directorStorage.getById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getName()).isEqualTo("Нолан");
    }

    @Test
    void testGetByIdNotFound() {
        Optional<Director> found = directorStorage.getById(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testGetAll() {
        jdbc.update("DELETE FROM directors");

        Director d1 = new Director();
        d1.setName("Режиссёр 1");
        directorStorage.create(d1);

        Director d2 = new Director();
        d2.setName("Режиссёр 2");
        directorStorage.create(d2);

        List<Director> directors = directorStorage.getAll();
        assertThat(directors).hasSize(2);
    }

    @Test
    void testUpdate() {
        Director director = new Director();
        director.setName("Original");
        Director saved = directorStorage.create(director);

        saved.setName("Updated");
        directorStorage.update(saved);

        Optional<Director> found = directorStorage.getById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated");
    }

    @Test
    void testDelete() {
        Director director = new Director();
        director.setName("ToDelete");
        Director saved = directorStorage.create(director);

        directorStorage.delete(saved.getId());

        assertThat(directorStorage.getById(saved.getId())).isEmpty();
    }
}