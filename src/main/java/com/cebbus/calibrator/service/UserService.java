package com.cebbus.calibrator.service;

import com.cebbus.calibrator.domain.User;

public interface UserService extends Service<User> {

    User get(String username);

    void delete(Long id);

}
