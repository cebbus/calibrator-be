package com.cebbus.calibrator.repository;

import com.cebbus.calibrator.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

}
