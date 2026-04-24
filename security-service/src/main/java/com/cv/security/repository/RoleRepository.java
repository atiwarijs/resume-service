package com.cv.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cv.security.entity.UserRole;

@Repository
public interface RoleRepository extends JpaRepository<UserRole, Long> {

}
