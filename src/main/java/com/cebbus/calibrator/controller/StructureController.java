package com.cebbus.calibrator.controller;

import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.service.StructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/structures")
public class StructureController {

    private final StructureService service;

    @GetMapping
    public List<Structure> list() {
        return service.list();
    }

    @PostMapping
    public Structure save(@RequestBody Structure structure) {
        return service.save(structure);
    }

    @PutMapping("/{id}")
    public Structure update(@RequestBody Structure structure) {
        return service.update(structure);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/{id}")
    public Structure generate(@PathVariable Long id) {
        return service.generate(id);
    }

}
