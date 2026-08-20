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
@RequiredArgsConstructor
class DirectorDbStorageTest {
    private final DirectorDbStorage directorStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM film_directors");
        jdbc.update("DELETE FROM directors");
    }

    @Test
    void testFindAll() {
        Director d1 = new Director();
        d1.setName("Director 1");
        directorStorage.add(d1);

        Director d2 = new Director();
        d2.setName("Director 2");
        directorStorage.add(d2);

        List<Director> directors = directorStorage.findAll();

        assertThat(directors).hasSize(2);
    }

    @Test
    void testFindById() {
        Director director = new Director();
        director.setName("Test Director");
        Director saved = directorStorage.add(director);

        Optional<Director> found = directorStorage.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getName()).isEqualTo("Test Director");
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Director> found = directorStorage.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    void testAdd() {
        Director director = new Director();
        director.setName("New Director");
        Director saved = directorStorage.add(director);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Director");
    }

    @Test
    void testUpdate() {
        Director director = new Director();
        director.setName("Original");
        Director saved = directorStorage.add(director);

        saved.setName("Updated");
        directorStorage.update(saved);

        Optional<Director> found = directorStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated");
    }

    @Test
    void testDelete() {
        Director director = new Director();
        director.setName("ToDelete");
        Director saved = directorStorage.add(director);

        directorStorage.delete(saved.getId());

        assertThat(directorStorage.findById(saved.getId())).isEmpty();
    }
}
