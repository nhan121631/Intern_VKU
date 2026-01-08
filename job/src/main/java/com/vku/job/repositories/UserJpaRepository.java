package com.vku.job.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vku.job.entities.User;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    
}
