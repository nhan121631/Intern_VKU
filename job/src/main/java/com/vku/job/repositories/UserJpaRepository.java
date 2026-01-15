package com.vku.job.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.vku.job.entities.User;
import com.vku.job.repositories.projection.FullNameUserProjection;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndIsActive(String username, int isActive);

    Optional<User> findByUsername(String username);

    boolean existsByEmailAndEmailVerifiedTrue(String email);

    boolean existsByEmailAndEmailVerifiedTrueAndPasswordIsNotNull(String email);

    Optional<User> findByEmail(String email);

    boolean existsByUsernameAndEmailVerifiedTrue(String username);

    @Query("SELECT u.id as id, u.profile.fullName as fullName FROM User u ")
    List<FullNameUserProjection> getAllFullNameUser();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'Users'")
    Page<User> findAll(Pageable pageable);

}
