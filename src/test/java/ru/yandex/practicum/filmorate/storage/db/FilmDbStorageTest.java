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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_directors");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM directors");
    }

    private Director createDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return directorStorage.create(director); // ВАЖНО: используем create, а не add
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
        assertThat(saved.getLikes()).isEmpty();
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
    void testAddWithDirectors() {
        Director d1 = createDirector("Режиссёр 1");
        Director d2 = createDirector("Режиссёр 2");

        Film film = createFilm("WithDirectors");
        film.setDirectors(Set.of(d1, d2));

        Film saved = filmStorage.add(film);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDirectors()).hasSize(2);
        assertThat(saved.getDirectors()).extracting(Director::getName)
                .containsExactlyInAnyOrder("Режиссёр 1", "Режиссёр 2");
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
    void testUpdateDirectors() {
        Director d1 = createDirector("Режиссёр 1");
        Film film = createFilm("WithDirectors");
        film.setDirectors(Set.of(d1));
        Film saved = filmStorage.add(film);

        Director d2 = createDirector("Режиссёр 2");
        saved.setDirectors(Set.of(d2));
        filmStorage.update(saved);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDirectors()).hasSize(1);
        assertThat(found.get().getDirectors()).extracting(Director::getName).containsExactly("Режиссёр 2");
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
    void testAddLike() {
        Film film = createFilm("Liked");
        Film saved = filmStorage.add(film);
        User user = createUser("user1");

        filmStorage.addLike(saved.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).containsExactly(user.getId());
    }

    @Test
    void testRemoveLike() {
        Film film = createFilm("Unliked");
        Film saved = filmStorage.add(film);
        User user = createUser("user2");

        filmStorage.addLike(saved.getId(), user.getId());
        filmStorage.removeLike(saved.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).isEmpty();
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

        filmStorage.addLike(saved1.getId(), u1.getId());
        filmStorage.addLike(saved1.getId(), u2.getId());
        filmStorage.addLike(saved2.getId(), u3.getId());

        List<Film> popular = filmStorage.getPopular(10, null, null);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(saved1.getId());
        assertThat(popular.get(0).getLikes()).hasSize(2);
        assertThat(popular.get(1).getId()).isEqualTo(saved2.getId());
        assertThat(popular.get(1).getLikes()).hasSize(1);
    }

    @Test
    void testGetPopularLimit() {
        for (int i = 0; i < 5; i++) {
            Film film = createFilm("Film" + i);
            Film saved = filmStorage.add(film);
            User user = createUser("u" + i);
            filmStorage.addLike(saved.getId(), user.getId());
        }

        List<Film> popular = filmStorage.getPopular(3, null, null);
        assertThat(popular).hasSize(3);
    }

    @Test
    void testGetPopularFilteredByGenreAndYear() {
        Film film1 = createFilm("Old Comedy");
        film1.setReleaseDate(LocalDate.of(1990, 5, 1));
        film1.setGenres(Set.of(new Genre(1L, "Комедия")));
        Film saved1 = filmStorage.add(film1);

        Film film2 = createFilm("New Comedy");
        film2.setReleaseDate(LocalDate.of(2020, 5, 1));
        film2.setGenres(Set.of(new Genre(1L, "Комедия")));
        Film saved2 = filmStorage.add(film2);

        Film film3 = createFilm("New Drama");
        film3.setReleaseDate(LocalDate.of(2020, 6, 1));
        film3.setGenres(Set.of(new Genre(2L, "Драма")));
        Film saved3 = filmStorage.add(film3);

        User u1 = createUser("u1");
        User u2 = createUser("u2");
        User u3 = createUser("u3");

        filmStorage.addLike(saved3.getId(), u1.getId());
        filmStorage.addLike(saved3.getId(), u2.getId());
        filmStorage.addLike(saved3.getId(), u3.getId());
        filmStorage.addLike(saved2.getId(), u1.getId());
        filmStorage.addLike(saved2.getId(), u2.getId());
        filmStorage.addLike(saved1.getId(), u1.getId());

        List<Film> popularComedies = filmStorage.getPopular(10, 1L, null);
        assertThat(popularComedies).hasSize(2);
        assertThat(popularComedies.get(0).getId()).isEqualTo(saved2.getId());

        List<Film> popular2020 = filmStorage.getPopular(10, null, 2020);
        assertThat(popular2020).hasSize(2);
        assertThat(popular2020.get(0).getId()).isEqualTo(saved3.getId());

        List<Film> popularComedy2020 = filmStorage.getPopular(10, 1L, 2020);
        assertThat(popularComedy2020).hasSize(1);
        assertThat(popularComedy2020.get(0).getId()).isEqualTo(saved2.getId());
    }

    @Test
    void testFindByIdWithGenresAndLikes() {
        Film film = createFilm("FullFilm");
        Genre g = new Genre();
        g.setId(1L);
        film.setGenres(Set.of(g));
        Film saved = filmStorage.add(film);

        User user = createUser("liker");
        filmStorage.addLike(saved.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).hasSize(1);
        assertThat(found.get().getLikes()).hasSize(1);
    }

    @Test
    void testFindByIdWithDirectors() {
        Director d = createDirector("Тарантино");
        Film film = createFilm("WithDirectors");
        film.setDirectors(Set.of(d));
        Film saved = filmStorage.add(film);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDirectors()).hasSize(1);
        assertThat(found.get().getDirectors()).extracting(Director::getName).containsExactly("Тарантино");
    }

    @Test
    void testGetFilmsByDirectorSortByYear() {
        Director d = createDirector("Спилберг");

        Film film1 = createFilm("Парк юрского периода");
        film1.setReleaseDate(LocalDate.of(1993, 6, 11));
        film1.setDirectors(Set.of(d));
        Film saved1 = filmStorage.add(film1);

        Film film2 = createFilm("Парк юрского периода 2");
        film2.setReleaseDate(LocalDate.of(1997, 5, 23));
        film2.setDirectors(Set.of(d));
        Film saved2 = filmStorage.add(film2);

        List<Film> films = filmStorage.getFilmsByDirector(d.getId(), "year");

        assertThat(films).hasSize(2);
        assertThat(films.get(0).getId()).isEqualTo(saved1.getId());
        assertThat(films.get(1).getId()).isEqualTo(saved2.getId());
    }

    @Test
    void testGetFilmsByDirectorSortByLikes() {
        Director d = createDirector("Нолан");

        Film film1 = createFilm("Интерстеллар");
        film1.setDirectors(Set.of(d));
        Film saved1 = filmStorage.add(film1);

        Film film2 = createFilm("Довод");
        film2.setDirectors(Set.of(d));
        Film saved2 = filmStorage.add(film2);

        User u1 = createUser("u1");
        User u2 = createUser("u2");

        filmStorage.addLike(saved1.getId(), u1.getId());
        filmStorage.addLike(saved1.getId(), u2.getId());
        filmStorage.addLike(saved2.getId(), u1.getId());

        List<Film> films = filmStorage.getFilmsByDirector(d.getId(), "likes");

        assertThat(films).hasSize(2);
        assertThat(films.get(0).getId()).isEqualTo(saved1.getId());
        assertThat(films.get(0).getLikes()).hasSize(2);
        assertThat(films.get(1).getId()).isEqualTo(saved2.getId());
        assertThat(films.get(1).getLikes()).hasSize(1);
    }

    @Test
    void testSearchByTitle() {
        filmStorage.add(createFilm("Крадущийся тигр"));
        filmStorage.add(createFilm("Крадущийся в ночи"));
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
        f1.setDirectors(Set.of(d));
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

        Film f1 = createFilm("Интерстеллар");
        f1.setDirectors(Set.of(d2));
        filmStorage.add(f1);

        Film f2 = createFilm("Довод");
        f2.setDirectors(Set.of(d1));
        filmStorage.add(f2);

        filmStorage.add(createFilm("Форсаж"));

        List<Film> result = filmStorage.search("н", "director,title");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Film::getName)
                .containsExactlyInAnyOrder("Интерстеллар", "Довод");
    }

    @Test
    void testSearchCaseInsensitive() {
        filmStorage.add(createFilm("Крадущийся тигр"));

        List<Film> result = filmStorage.search("КРАД", "title");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Крадущийся тигр");
    }

    @Test
    void testSearchSortedByPopularity() {
        Director d = createDirector("Режиссёр");

        Film f1 = createFilm("Популярный");
        f1.setDirectors(Set.of(d));
        Film saved1 = filmStorage.add(f1);

        Film f2 = createFilm("Популярный слегка");
        f2.setDirectors(Set.of(d));
        Film saved2 = filmStorage.add(f2);

        User u1 = createUser("u1");
        User u2 = createUser("u2");
        filmStorage.addLike(saved1.getId(), u1.getId());
        filmStorage.addLike(saved1.getId(), u2.getId());
        filmStorage.addLike(saved2.getId(), u1.getId());

        List<Film> result = filmStorage.search("популярн", "title");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLikes()).hasSize(2);
        assertThat(result.get(1).getLikes()).hasSize(1);
    }
}
