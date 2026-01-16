package com.vku.job.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vku.job.entities.User;
import com.vku.job.entities.UserProfile;

@Repository
public interface UserProfileJpaRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser(User user);

    Optional<UserProfile> findByUserId(Long userId);

    boolean existsByPhoneNumberAndUserIdNot(String phoneNumber, Long userId);

}
