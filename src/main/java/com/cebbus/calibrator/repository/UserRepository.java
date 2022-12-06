package com.cebbus.calibrator.repository;

import com.cebbus.calibrator.domain.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    User findByUsername(String username);

}
