package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
<<<<<<< HEAD

    Director create(Director director);
=======
    List<Director> findAll();

    Optional<Director> findById(Long id);

    Director add(Director director);
>>>>>>> develop

    Director update(Director director);

    void delete(Long id);
<<<<<<< HEAD

    Optional<Director> getById(Long id);

    List<Director> getAll();
}
=======
}
>>>>>>> develop
