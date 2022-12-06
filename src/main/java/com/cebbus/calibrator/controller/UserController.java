package com.cebbus.calibrator.controller;

import com.cebbus.calibrator.domain.User;
import com.cebbus.calibrator.filter.SortWrapper;
import com.cebbus.calibrator.filter.SpecificationBuilder;
import com.cebbus.calibrator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    @GetMapping
    public Page<User> list(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter) {
        PageRequest pageRequest = PageRequest.of(--page, limit, SortWrapper.valueOf(sort));

        SpecificationBuilder<User> builder = new SpecificationBuilder<>(User.class);
        builder.with(filter);

        return service.getPage(builder.build(), pageRequest);
    }

    @PostMapping
    public User save(@RequestBody User user) {
        return service.save(user);
    }

    @PutMapping("/{id}")
    public User update(@RequestBody User user) {
        return service.update(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

}
