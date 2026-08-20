package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaRatingDbStorage.class)
class MpaRatingDbStorageTest {

    @Autowired
    private MpaRatingDbStorage mpaStorage;

    @Test
    void testFindAll() {
        List<MpaRating> ratings = mpaStorage.findAll();
        assertThat(ratings).hasSize(5);
        assertThat(ratings).extracting(MpaRating::getId).containsExactly(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    void testFindAllNames() {
        List<MpaRating> ratings = mpaStorage.findAll();
        assertThat(ratings).extracting(MpaRating::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testFindById() {
        Optional<MpaRating> rating = mpaStorage.findById(3L);
        assertThat(rating).isPresent();
        assertThat(rating.get().getId()).isEqualTo(3L);
        assertThat(rating.get().getName()).isEqualTo("PG-13");
    }

    @Test
    void testFindByIdNotFound() {
        Optional<MpaRating> rating = mpaStorage.findById(9999L);
        assertThat(rating).isEmpty();
    }
}