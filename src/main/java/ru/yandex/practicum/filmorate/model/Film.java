package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.validation.ReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "films")
@Getter
@Setter
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    @Column(nullable = false)
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    @Column(length = 200)
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой")
    @ReleaseDate
    @Column(nullable = false)
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    @Column(nullable = false)
    private Long duration;

    @ManyToMany
    @JoinTable(
        name = "film_genres",
        joinColumns = @JoinColumn(name = "film_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "film_directors",
        joinColumns = @JoinColumn(name = "film_id"),
        inverseJoinColumns = @JoinColumn(name = "director_id")
    )
    @JsonAlias("director")
    private Set<Director> directors = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "mpa_rating_id")
    @NotNull(message = "Рейтинг MPA обязателен")
    private MpaRating mpa;

    @Transient
    private Double rating;

    @JsonProperty("rate")
    public Double getRate() {
        return rating;
    }
}
