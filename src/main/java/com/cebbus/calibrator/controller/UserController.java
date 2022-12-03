package com.cebbus.calibrator.controller;

import com.cebbus.calibrator.common.BeanOperations;
import com.cebbus.calibrator.domain.User;
import com.cebbus.calibrator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final BeanOperations beanOperations;

    @GetMapping
    public List<User> list() {
        return userService.list();
    }

    @PostMapping
    public User save(@RequestBody User user) {
        return userService.save(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        User dbUser = userService.get(id);
        BeanUtils.copyProperties(user, dbUser, beanOperations.getNullPropertyNames(user));

        return userService.update(dbUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

}
