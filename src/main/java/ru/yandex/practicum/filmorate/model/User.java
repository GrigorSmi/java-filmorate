package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "email")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ @")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    @Column(nullable = false)
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не может быть пустой")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @Column(nullable = false)
    private LocalDate birthday;
}
