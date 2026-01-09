package com.vku.job.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.vku.job.entities.User;
import com.vku.job.repositories.projection.FullNameUserProjection;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u.id as id, u.fullName as fullName FROM User u")
    List<FullNameUserProjection> getAllFullNameUser();

}
