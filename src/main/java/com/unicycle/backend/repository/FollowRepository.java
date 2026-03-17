package com.unicycle.backend.repository;

import com.unicycle.backend.model.Follow;
import com.unicycle.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Bir kullanıcının takipçilerini (Followers) bul
    List<Follow> findByFollowing(User following);

    // Bir kullanıcının takip ettiklerini (Following) bul
    List<Follow> findByFollower(User follower);

    // A kişisi B kişisini zaten takip ediyor mu kontrolü (Çift tıklamayı önler)
    boolean existsByFollowerAndFollowing(User follower, User following);

    // Takipten çıkmak için
    void deleteByFollowerAndFollowing(User follower, User following);
}