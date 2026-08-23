package ru.yandex.practicum.filmorate.storage.db;

<<<<<<< HEAD
=======
import lombok.RequiredArgsConstructor;
>>>>>>> develop
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;

<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> develop
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(DirectorDbStorage.class)
<<<<<<< HEAD
class DirectorDbStorageTest {

    @Autowired
    private DirectorDbStorage directorStorage;

    @Autowired
    private JdbcTemplate jdbc;
=======
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DirectorDbStorageTest {
    private final DirectorDbStorage directorStorage;
    private final JdbcTemplate jdbc;
>>>>>>> develop

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM film_directors");
        jdbc.update("DELETE FROM directors");
    }

    @Test
<<<<<<< HEAD
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
        assertThat(found.get().getName()).isEqualTo("Нолан");
    }

    @Test
    void testGetByIdNotFound() {
        Optional<Director> found = directorStorage.getById(9999L);
=======
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

>>>>>>> develop
        assertThat(found).isEmpty();
    }

    @Test
<<<<<<< HEAD
    void testGetAll() {
        jdbc.update("DELETE FROM directors");

        Director d1 = new Director();
        d1.setName("Режиссёр 1");
        directorStorage.create(d1);

        Director d2 = new Director();
        d2.setName("Режиссёр 2");
        directorStorage.create(d2);

        assertThat(directorStorage.getAll()).hasSize(2);
=======
    void testAdd() {
        Director director = new Director();
        director.setName("New Director");
        Director saved = directorStorage.add(director);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Director");
>>>>>>> develop
    }

    @Test
    void testUpdate() {
        Director director = new Director();
<<<<<<< HEAD
        director.setName("Старое имя");
        Director saved = directorStorage.create(director);
        saved.setName("Новое имя");
        Director updated = directorStorage.update(saved);
        assertThat(updated.getName()).isEqualTo("Новое имя");
=======
        director.setName("Original");
        Director saved = directorStorage.add(director);

        saved.setName("Updated");
        directorStorage.update(saved);

        Optional<Director> found = directorStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated");
>>>>>>> develop
    }

    @Test
    void testDelete() {
        Director director = new Director();
<<<<<<< HEAD
        director.setName("Удалить");
        Director saved = directorStorage.create(director);
        directorStorage.delete(saved.getId());
        assertThat(directorStorage.getById(saved.getId())).isEmpty();
    }
}
=======
        director.setName("ToDelete");
        Director saved = directorStorage.add(director);

        directorStorage.delete(saved.getId());

        assertThat(directorStorage.findById(saved.getId())).isEmpty();
    }
}
>>>>>>> develop
