package com.cebbus.calibrator.controller;

import com.cebbus.calibrator.common.ClassOperations;
import com.cebbus.calibrator.common.CustomClassOperations;
import com.cebbus.calibrator.domain.enums.DataType;
import com.cebbus.calibrator.filter.SortWrapper;
import com.cebbus.calibrator.filter.SpecificationBuilder;
import com.cebbus.calibrator.service.DataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/data")
public class DataController {

    private final DataService service;
    private final CustomClassOperations operations;

    @GetMapping("/{name}")
    public <T> Page<T> list(
            @PathVariable String name,
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter) {
        Class<T> type = getType(name);
        PageRequest pageRequest = PageRequest.of(--page, limit, SortWrapper.valueOf(sort));

        SpecificationBuilder<T> builder = new SpecificationBuilder<>(type);
        builder.with(filter);

        return service.getPage(builder.build(), pageRequest, type);
    }

    @PostMapping("/{name}")
    public <T> T save(@PathVariable String name, @RequestBody Map<String, Object> payload) {
        T instance = createInstance(name, payload);
        return service.save(instance);
    }

    @PutMapping("/{name}/{id}")
    public <T> T update(@PathVariable String name, @RequestBody Map<String, Object> payload) {
        T instance = createInstance(name, payload);
        return service.update(instance);
    }

    @DeleteMapping("/{name}/{id}")
    public <T> ResponseEntity<?> delete(@PathVariable String name, @RequestBody Map<String, Object> payload) {
        T instance = createInstance(name, payload);
        service.delete(instance);
        return ResponseEntity.ok().build();
    }

    private <T> Class<T> getType(String customClassName) {
        return operations.resolveCustomClass(customClassName);
    }

    private <T> T createInstance(String name, Map<String, Object> payload) {
        Class<T> type = getType(name);
        T instance = ClassOperations.createInstance(type);

        payload.forEach((k, v) -> {
            try {
                Field field = ClassOperations.findField(type, k);

                if (v == null) {
                    ClassOperations.setField(instance, k, null);
                } else if (field.getType().equals(Long.class)) {
                    ClassOperations.setField(instance, k, Long.valueOf(v.toString()));
                } else {
                    DataType dataType = DataType.valueOf(field.getType());
                    ClassOperations.setField(instance, k, dataType.getParser().apply(v.toString()));
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        });

        return instance;
    }

}
