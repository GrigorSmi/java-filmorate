package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(DirectorDbStorage.class)
class DirectorDbStorageTest {

    @Autowired
    private DirectorDbStorage directorStorage;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void testAdd() {
        Director director = new Director();
        director.setName("Тарантино");
        Director saved = directorStorage.add(director);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void testFindById() {
        Director director = new Director();
        director.setName("Нолан");
        Director saved = directorStorage.add(director);
        Optional<Director> found = directorStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Нолан");
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Director> found = directorStorage.findById(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll() {
        jdbc.update("DELETE FROM directors");
        directorStorage.add(new Director(null, "Режиссёр 1"));
        directorStorage.add(new Director(null, "Режиссёр 2"));
        assertThat(directorStorage.findAll()).hasSize(2);
    }

    @Test
    void testUpdate() {
        Director director = new Director();
        director.setName("Старое имя");
        Director saved = directorStorage.add(director);
        saved.setName("Новое имя");
        Director updated = directorStorage.update(saved);
        assertThat(updated.getName()).isEqualTo("Новое имя");
    }

    @Test
    void testDelete() {
        Director director = new Director();
        director.setName("Удалить");
        Director saved = directorStorage.add(director);
        directorStorage.delete(saved.getId());
        assertThat(directorStorage.findById(saved.getId())).isEmpty();
    }
}