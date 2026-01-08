package com.vku.job.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vku.job.entities.Role;

public interface RoleJpaRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
