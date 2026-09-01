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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class, DirectorDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final DirectorDbStorage directorStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM marks");
        jdbc.update("DELETE FROM film_directors");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM directors");
    }

    private Director createDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return directorStorage.add(director);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description of " + name);
        film.setReleaseDate(LocalDate.of(2000, 6, 15));
        film.setDuration(120L);
        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        film.setMpa(mpa);
        return film;
    }

    private User createUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@mail.com");
        user.setName("name_" + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.add(user);
    }

    @Test
    void testAdd() {
        Film film = createFilm("TestFilm");
        Film saved = filmStorage.add(film);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("TestFilm");
        assertThat(saved.getMpa()).isNotNull();
        assertThat(saved.getMpa().getName()).isEqualTo("G");
        assertThat(saved.getGenres()).isEmpty();
        assertThat(saved.getRating()).isNull();
    }

    @Test
    void testAddWithGenres() {
        Film film = createFilm("WithGenres");
        Genre genre1 = new Genre();
        genre1.setId(1L);
        Genre genre2 = new Genre();
        genre2.setId(2L);
        film.setGenres(new HashSet<>(List.of(genre1, genre2)));

        Film saved = filmStorage.add(film);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getGenres()).hasSize(2);
        assertThat(saved.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void testFindById() {
        Film film = createFilm("FindMe");
        Film saved = filmStorage.add(film);

        Optional<Film> found = filmStorage.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getName()).isEqualTo("FindMe");
        assertThat(found.get().getMpa()).isNotNull();
        assertThat(found.get().getMpa().getName()).isEqualTo("G");
        assertThat(found.get().getGenres()).isEmpty();
        assertThat(found.get().getRating()).isNull();
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Film> found = filmStorage.findById(9999L);

        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll() {
        filmStorage.add(createFilm("Film1"));
        filmStorage.add(createFilm("Film2"));
        filmStorage.add(createFilm("Film3"));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(3);
    }

    @Test
    void testUpdate() {
        Film film = createFilm("Original");
        Film saved = filmStorage.add(film);

        saved.setName("Updated");
        saved.setDescription("Updated desc");
        saved.setDuration(200L);
        Film updated = filmStorage.update(saved);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getDescription()).isEqualTo("Updated desc");
        assertThat(updated.getDuration()).isEqualTo(200L);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated");
    }

    @Test
    void testUpdateGenres() {
        Film film = createFilm("WithGenres");
        Genre g1 = new Genre();
        g1.setId(1L);
        film.setGenres(new HashSet<>(List.of(g1)));
        Film saved = filmStorage.add(film);

        Genre g2 = new Genre();
        g2.setId(2L);
        saved.setGenres(new HashSet<>(List.of(g2)));
        filmStorage.update(saved);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).hasSize(1);
        assertThat(found.get().getGenres()).extracting(Genre::getId).containsExactly(2L);
    }

    @Test
    void testDelete() {
        Film film = createFilm("ToDelete");
        Film saved = filmStorage.add(film);

        filmStorage.delete(saved.getId());

        assertThat(filmStorage.findById(saved.getId())).isEmpty();
    }

    @Test
    void testClearAll() {
        filmStorage.add(createFilm("F1"));
        filmStorage.add(createFilm("F2"));

        filmStorage.clearAll();

        assertThat(filmStorage.findAll()).isEmpty();
    }

    @Test
    void testAddMark() {
        Film film = createFilm("Liked");
        Film saved = filmStorage.add(film);
        User user = createUser("user1");

        filmStorage.addMark(saved.getId(), user.getId(), 10);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRating()).isEqualTo(10.0);
    }

    @Test
    void testRemoveMark() {
        Film film = createFilm("Unliked");
        Film saved = filmStorage.add(film);
        User user = createUser("user2");

        filmStorage.addMark(saved.getId(), user.getId(), 10);
        filmStorage.removeMark(saved.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRating()).isNull();
    }

    @Test
    void testGetPopular() {
        Film film1 = createFilm("Popular");
        Film saved1 = filmStorage.add(film1);
        Film film2 = createFilm("NotPopular");
        Film saved2 = filmStorage.add(film2);

        User u1 = createUser("u1");
        User u2 = createUser("u2");
        User u3 = createUser("u3");

        filmStorage.addMark(saved1.getId(), u1.getId(), 10);
        filmStorage.addMark(saved1.getId(), u2.getId(), 8);
        filmStorage.addMark(saved2.getId(), u3.getId(), 3);

        List<Film> popular = filmStorage.getPopular(10, null, null);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(saved1.getId());
        assertThat(popular.get(0).getRating()).isEqualTo(9.0);
        assertThat(popular.get(1).getId()).isEqualTo(saved2.getId());
        assertThat(popular.get(1).getRating()).isEqualTo(3.0);
    }

    @Test
    void testGetPopularWithGenreAndYear() {
        Film film1 = createFilm("Popular");
        Genre g = new Genre();
        g.setId(1L);
        film1.setGenres(new HashSet<>(Set.of(g)));
        film1.setReleaseDate(LocalDate.of(2010, 5, 10));
        Film saved1 = filmStorage.add(film1);

        Film film2 = createFilm("Other");
        film2.setReleaseDate(LocalDate.of(2010, 5, 10));
        Film saved2 = filmStorage.add(film2);

        User u1 = createUser("u1");
        filmStorage.addMark(saved1.getId(), u1.getId(), 10);
        filmStorage.addMark(saved2.getId(), u1.getId(), 1);

        List<Film> popular = filmStorage.getPopular(10, 1L, 2010);

        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).getId()).isEqualTo(saved1.getId());
    }

    @Test
    void testGetPopularSortsByAverageRating() {
        // значения 1-5 считаются отрицательными, но сортировка всё равно по средней оценке
        Film film1 = createFilm("F1");
        Film saved1 = filmStorage.add(film1);
        Film film2 = createFilm("F2");
        Film saved2 = filmStorage.add(film2);

        User u1 = createUser("u1");
        User u2 = createUser("u2");
        filmStorage.addMark(saved1.getId(), u1.getId(), 10);
        filmStorage.addMark(saved1.getId(), u2.getId(), 3);
        filmStorage.addMark(saved2.getId(), u1.getId(), 10);

        List<Film> popular = filmStorage.getPopular(10, null, null);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getRating()).isEqualTo(10.0);
        assertThat(popular.get(1).getRating()).isEqualTo(6.5);
    }

    @Test
    void testGetPopularLimit() {
        for (int i = 0; i < 5; i++) {
            Film film = createFilm("Film" + i);
            Film saved = filmStorage.add(film);
            User user = createUser("u" + i);
            filmStorage.addMark(saved.getId(), user.getId(), 10);
        }

        List<Film> popular = filmStorage.getPopular(3, null, null);

        assertThat(popular).hasSize(3);
    }

    @Test
    void testGetCommonFilms() {
        Film film1 = createFilm("Common1");
        Film saved1 = filmStorage.add(film1);
        Film film2 = createFilm("Common2");
        Film saved2 = filmStorage.add(film2);
        Film film3 = createFilm("OnlyUser1");
        Film saved3 = filmStorage.add(film3);

        User u1 = createUser("u1");
        User u2 = createUser("u2");
        filmStorage.addMark(saved1.getId(), u1.getId(), 10);
        filmStorage.addMark(saved1.getId(), u2.getId(), 8);
        filmStorage.addMark(saved2.getId(), u1.getId(), 7);
        filmStorage.addMark(saved2.getId(), u2.getId(), 6);
        filmStorage.addMark(saved3.getId(), u1.getId(), 10);

        List<Film> common = filmStorage.getCommonFilms(u1.getId(), u2.getId());

        assertThat(common).hasSize(2);
        assertThat(common.get(0).getId()).isEqualTo(saved1.getId());
        assertThat(common.get(1).getId()).isEqualTo(saved2.getId());
    }

    @Test
    void testGetCommonFilmsEmptyWhenNoCommon() {
        Film film1 = createFilm("OnlyUser1");
        Film saved1 = filmStorage.add(film1);
        Film film2 = createFilm("OnlyUser2");
        Film saved2 = filmStorage.add(film2);

        User u1 = createUser("u1");
        User u2 = createUser("u2");
        filmStorage.addMark(saved1.getId(), u1.getId(), 10);
        filmStorage.addMark(saved2.getId(), u2.getId(), 10);

        List<Film> common = filmStorage.getCommonFilms(u1.getId(), u2.getId());

        assertThat(common).isEmpty();
    }

    @Test
    void testFindByIdWithGenresAndLikes() {
        Film film = createFilm("FullFilm");
        Genre g = new Genre();
        g.setId(1L);
        film.setGenres(new HashSet<>(Set.of(g)));
        Film saved = filmStorage.add(film);

        User user = createUser("liker");
        filmStorage.addMark(saved.getId(), user.getId(), 10);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).hasSize(1);
        assertThat(found.get().getRating()).isNotNull();
    }

    @Test
    void testAddWithDirectors() {
        Director d1 = createDirector("Director 1");
        Director d2 = createDirector("Director 2");

        Film film = createFilm("WithDirectors");
        film.setDirectors(new HashSet<>(List.of(d1, d2)));

        Film saved = filmStorage.add(film);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDirectors()).hasSize(2);
        assertThat(saved.getDirectors()).extracting(Director::getId).containsExactlyInAnyOrder(d1.getId(), d2.getId());
    }

    @Test
    void testFindByIdWithDirectors() {
        Director d = createDirector("Director");
        Film film = createFilm("WithDirectors");
        film.setDirectors(new HashSet<>(Set.of(d)));
        Film saved = filmStorage.add(film);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDirectors()).hasSize(1);
        assertThat(found.get().getDirectors().iterator().next().getId()).isEqualTo(d.getId());
    }

    @Test
    void testUpdateDirectors() {
        Director d1 = createDirector("Director 1");
        Film film = createFilm("WithDirectors");
        film.setDirectors(new HashSet<>(List.of(d1)));
        Film saved = filmStorage.add(film);

        Director d2 = createDirector("Director 2");
        saved.setDirectors(new HashSet<>(List.of(d2)));
        filmStorage.update(saved);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDirectors()).hasSize(1);
        assertThat(found.get().getDirectors()).extracting(Director::getId).containsExactly(d2.getId());
    }

    @Test
    void testGetFilmsByDirectorSortByYear() {
        Director director = createDirector("Director");

        Film film1 = createFilm("Film2020");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDirectors(new HashSet<>(Set.of(director)));
        Film saved1 = filmStorage.add(film1);

        Film film2 = createFilm("Film2010");
        film2.setReleaseDate(LocalDate.of(2010, 1, 1));
        film2.setDirectors(new HashSet<>(Set.of(director)));
        Film saved2 = filmStorage.add(film2);

        List<Film> result = filmStorage.getFilmsByDirector(director.getId(), "year");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReleaseDate()).isBefore(result.get(1).getReleaseDate());
    }

    @Test
    void testGetFilmsByDirectorSortByLikes() {
        Director director = createDirector("Director");

        Film film1 = createFilm("Popular");
        film1.setDirectors(new HashSet<>(Set.of(director)));
        Film saved1 = filmStorage.add(film1);

        Film film2 = createFilm("NotPopular");
        film2.setDirectors(new HashSet<>(Set.of(director)));
        Film saved2 = filmStorage.add(film2);

        User u1 = createUser("u1");
        User u2 = createUser("u2");
        filmStorage.addMark(saved1.getId(), u1.getId(), 10);
        filmStorage.addMark(saved1.getId(), u2.getId(), 8);
        filmStorage.addMark(saved2.getId(), u1.getId(), 3);

        List<Film> result = filmStorage.getFilmsByDirector(director.getId(), "likes");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(saved1.getId());
        assertThat(result.get(0).getRating()).isEqualTo(9.0);
        assertThat(result.get(1).getRating()).isEqualTo(3.0);
    }

    @Test
    void testGetFilmsByDirectorSortByRate() {
        Director director = createDirector("DirectorRate");

        Film filmNew = createFilm("Newer");
        filmNew.setReleaseDate(LocalDate.of(2020, 1, 1));
        filmNew.setDirectors(new HashSet<>(Set.of(director)));
        Film savedNew = filmStorage.add(filmNew);

        Film filmOld = createFilm("Older");
        filmOld.setReleaseDate(LocalDate.of(2010, 1, 1));
        filmOld.setDirectors(new HashSet<>(Set.of(director)));
        Film savedOld = filmStorage.add(filmOld);

        User u1 = createUser("u1");
        filmStorage.addMark(savedNew.getId(), u1.getId(), 10);
        filmStorage.addMark(savedOld.getId(), u1.getId(), 10);

        List<Film> result = filmStorage.getFilmsByDirector(director.getId(), "rate");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(savedNew.getId());
        assertThat(result.get(1).getId()).isEqualTo(savedOld.getId());
    }

    @Test
    void testSearchByTitle() {
        Film f1 = createFilm("Крадущийся тигр");
        filmStorage.add(f1);
        Film f2 = createFilm("Крадущийся в ночи");
        filmStorage.add(f2);
        filmStorage.add(createFilm("Звёздные войны"));

        List<Film> result = filmStorage.search("крад", "title");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Film::getName)
                .containsExactlyInAnyOrder("Крадущийся тигр", "Крадущийся в ночи");
    }

    @Test
    void testSearchByDirector() {
        Director d = createDirector("Тарантино");
        Film f1 = createFilm("Криминальное чтиво");
        f1.setDirectors(new HashSet<>(Set.of(d)));
        filmStorage.add(f1);
        filmStorage.add(createFilm("Звёздные войны"));

        List<Film> result = filmStorage.search("тарант", "director");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Криминальное чтиво");
    }

    @Test
    void testSearchByTitleAndDirector() {
        Director d1 = createDirector("Тарантино");
        Director d2 = createDirector("Нолан");

        // "Интерстеллар" содержит "н" в названии → match по title
        Film f1 = createFilm("Интерстеллар");
        f1.setDirectors(new HashSet<>(Set.of(d2)));
        filmStorage.add(f1);

        // "Довод" не содержит "н", но "Тарантино" содержит "н" → match по director
        Film f2 = createFilm("Довод");
        f2.setDirectors(new HashSet<>(Set.of(d1)));
        filmStorage.add(f2);

        // Не матчится ни по чему
        filmStorage.add(createFilm("Форсаж"));

        List<Film> result = filmStorage.search("н", "director,title");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Film::getName)
                .containsExactlyInAnyOrder("Интерстеллар", "Довод");
    }

    @Test
    void testSearchCaseInsensitive() {
        Film f = createFilm("Крадущийся тигр");
        filmStorage.add(f);

        List<Film> result = filmStorage.search("КРАД", "title");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Крадущийся тигр");
    }

    @Test
    void testSearchSortedByPopularity() {
        Director d = createDirector("Режиссёр");

        Film f1 = createFilm("Популярный");
        f1.setDirectors(new HashSet<>(Set.of(d)));
        Film saved1 = filmStorage.add(f1);

        Film f2 = createFilm("Популярный слегка");
        f2.setDirectors(new HashSet<>(Set.of(d)));
        Film saved2 = filmStorage.add(f2);

        User u1 = createUser("u1");
        User u2 = createUser("u2");
        filmStorage.addMark(saved1.getId(), u1.getId(), 10);
        filmStorage.addMark(saved1.getId(), u2.getId(), 8);
        filmStorage.addMark(saved2.getId(), u1.getId(), 3);

        List<Film> result = filmStorage.search("популярн", "title");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(saved1.getId());
        assertThat(result.get(0).getRating()).isEqualTo(9.0);
        assertThat(result.get(1).getRating()).isEqualTo(3.0);
    }
}
