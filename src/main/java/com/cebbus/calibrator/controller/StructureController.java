package com.cebbus.calibrator.controller;

import com.cebbus.calibrator.domain.Structure;
import com.cebbus.calibrator.filter.SortWrapper;
import com.cebbus.calibrator.filter.SpecificationBuilder;
import com.cebbus.calibrator.service.StructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/structures")
public class StructureController {

    private final StructureService service;

    @GetMapping
    public Page<Structure> list(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter) {
        PageRequest pageRequest = PageRequest.of(--page, limit, SortWrapper.valueOf(sort));

        SpecificationBuilder<Structure> builder = new SpecificationBuilder<>(Structure.class);
        builder.with(filter);

        return service.getPage(builder.build(), pageRequest);
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
