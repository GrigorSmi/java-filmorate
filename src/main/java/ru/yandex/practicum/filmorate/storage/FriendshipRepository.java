package ru.yandex.practicum.filmorate.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByUserId(Long userId);

    List<Friendship> findByFriendId(Long friendId);

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    void deleteByUserIdAndFriendId(Long userId, Long friendId);
}
