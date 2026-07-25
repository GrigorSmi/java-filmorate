package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {
    private final GenreDbStorage genreStorage;

    @Test
    void testFindAll() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).hasSize(6);
        assertThat(genres).extracting(Genre::getId).containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
    }

    @Test
    void testFindById() {
        Optional<Genre> genre = genreStorage.findById(1L);

        assertThat(genre).isPresent();
        assertThat(genre.get().getId()).isEqualTo(1L);
        assertThat(genre.get().getName()).isNotBlank();
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Genre> genre = genreStorage.findById(9999L);

        assertThat(genre).isEmpty();
    }

    @Test
    void testFindAllNamesAreNotEmpty() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).allMatch(g -> g.getName() != null && !g.getName().isBlank());
    }
}
