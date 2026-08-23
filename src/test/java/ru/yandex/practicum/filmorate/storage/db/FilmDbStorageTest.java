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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
<<<<<<< HEAD
import java.util.List;
import java.util.Optional;
import java.util.Set;
=======
import java.util.*;
>>>>>>> develop

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class, DirectorDbStorage.class})
<<<<<<< HEAD
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private DirectorDbStorage directorStorage;

    @Autowired
    private JdbcTemplate jdbc;

    // ... (импорты и начало класса как у тебя) ...
=======
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final DirectorDbStorage directorStorage;
    private final JdbcTemplate jdbc;

>>>>>>> develop
    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_directors");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM films");
<<<<<<< HEAD
        jdbc.update("DELETE FROM directors");
        jdbc.update("DELETE FROM users");
=======
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM directors");
>>>>>>> develop
    }

    private Director createDirector(String name) {
        Director director = new Director();
        director.setName(name);
<<<<<<< HEAD
        return directorStorage.create(director); // Исправлено с add на create
=======
        return directorStorage.add(director);
>>>>>>> develop
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
<<<<<<< HEAD
    void testAddWithDirectors() {
        Film film = createFilm("WithDirectors");
        Director d1 = createDirector("Режиссёр 1");
        Director d2 = createDirector("Режиссёр 2");
        film.setDirectors(Set.of(d1, d2));
=======
    void testAddWithGenres() {
        Film film = createFilm("WithGenres");
        Genre genre1 = new Genre();
        genre1.setId(1L);
        Genre genre2 = new Genre();
        genre2.setId(2L);
        film.setGenres(new HashSet<>(List.of(genre1, genre2)));
>>>>>>> develop

        Film saved = filmStorage.add(film);

        assertThat(saved.getId()).isNotNull();
<<<<<<< HEAD
        assertThat(saved.getDirectors()).hasSize(2);
        assertThat(saved.getDirectors()).extracting(Director::getName)
                .containsExactlyInAnyOrder("Режиссёр 1", "Режиссёр 2");
=======
        assertThat(saved.getGenres()).hasSize(2);
        assertThat(saved.getGenres()).extracting(Genre::getId).containsExactlyInAnyOrder(1L, 2L);
>>>>>>> develop
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
        assertThat(found.get().getLikes()).isEmpty();
    }

    @Test
<<<<<<< HEAD
=======
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

        List<Film> popular = filmStorage.getPopular(10);

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

        List<Film> popular = filmStorage.getPopular(3);

        assertThat(popular).hasSize(3);
    }

    @Test
>>>>>>> develop
    void testFindByIdWithGenresAndLikes() {
        Film film = createFilm("FullFilm");
        Genre g = new Genre();
        g.setId(1L);
<<<<<<< HEAD
        film.setGenres(Set.of(g));
=======
        film.setGenres(new HashSet<>(Set.of(g)));
>>>>>>> develop
        Film saved = filmStorage.add(film);

        User user = createUser("liker");
        filmStorage.addLike(saved.getId(), user.getId());

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).hasSize(1);
        assertThat(found.get().getLikes()).hasSize(1);
    }

    @Test
<<<<<<< HEAD
    void testFindByIdWithDirectors() {
        Film film = createFilm("WithDirectors");
        Director d = createDirector("Тарантино");
        film.setDirectors(Set.of(d));
=======
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
>>>>>>> develop
        Film saved = filmStorage.add(film);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDirectors()).hasSize(1);
<<<<<<< HEAD
        assertThat(found.get().getDirectors()).extracting(Director::getName).containsExactly("Тарантино");
=======
        assertThat(found.get().getDirectors().iterator().next().getId()).isEqualTo(d.getId());
>>>>>>> develop
    }

    @Test
    void testUpdateDirectors() {
<<<<<<< HEAD
        Film film = createFilm("WithDirectors");
        Director d1 = createDirector("Режиссёр 1");
        film.setDirectors(Set.of(d1));
        Film saved = filmStorage.add(film);

        Director d2 = createDirector("Режиссёр 2");
        saved.setDirectors(Set.of(d2));
=======
        Director d1 = createDirector("Director 1");
        Film film = createFilm("WithDirectors");
        film.setDirectors(new HashSet<>(List.of(d1)));
        Film saved = filmStorage.add(film);

        Director d2 = createDirector("Director 2");
        saved.setDirectors(new HashSet<>(List.of(d2)));
>>>>>>> develop
        filmStorage.update(saved);

        Optional<Film> found = filmStorage.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDirectors()).hasSize(1);
<<<<<<< HEAD
        assertThat(found.get().getDirectors()).extracting(Director::getName).containsExactly("Режиссёр 2");
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
        assertThat(films.get(1).getId()).isEqualTo(saved2.getId());
=======
        assertThat(found.get().getDirectors()).extracting(Director::getId).containsExactly(d2.getId());
>>>>>>> develop
    }

    @Test
    void testGetFilmsByDirectorSortByYear() {
<<<<<<< HEAD
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
}
=======
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
        filmStorage.addLike(saved1.getId(), u1.getId());
        filmStorage.addLike(saved1.getId(), u2.getId());
        filmStorage.addLike(saved2.getId(), u1.getId());

        List<Film> result = filmStorage.getFilmsByDirector(director.getId(), "likes");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLikes()).hasSize(2);
        assertThat(result.get(1).getLikes()).hasSize(1);
    }
}
>>>>>>> develop
