package com.cebbus.calibrator.init;

import com.cebbus.calibrator.domain.Role;
import com.cebbus.calibrator.domain.User;
import com.cebbus.calibrator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class InitUserRunner implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        long count = userService.count();
        if (count > 0) {
            return;
        }

        Role role = new Role();
        role.setName("ROLE_ADMIN");

        User user = new User();
        user.setName("admin");
        user.setSurname("admin");
        user.setUsername("admin");
        user.setPassword("secret");
        user.setEnabled(true);
        user.setRoles(Set.of(role));

        userService.save(user);
    }
}
