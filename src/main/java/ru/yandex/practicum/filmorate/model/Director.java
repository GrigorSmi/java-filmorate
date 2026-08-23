package ru.yandex.practicum.filmorate.model;

<<<<<<< HEAD
import lombok.Data;

@Data
public class Director {
    private Long id;
    private String name;
}
=======
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "directors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Director {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
>>>>>>> develop
