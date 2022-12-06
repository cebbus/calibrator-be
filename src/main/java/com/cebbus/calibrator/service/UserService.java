package com.cebbus.calibrator.service;

import com.cebbus.calibrator.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

public interface UserService extends Service<User> {

    User get(String username);

    Page<User> getPage(Specification<User> build, PageRequest pageRequest);

    long count();
}
