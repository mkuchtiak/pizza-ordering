package com.example.pizza.ordering.controllers;

import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public class Utils {

    private Utils() {
    }

    static <E, T> ResponseEntity<?> removeEntity(CrudRepository<E, T> crudRepository, T entityId) {
        Optional<E> opt = crudRepository.findById(entityId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            crudRepository.delete(opt.get());
            return ResponseEntity.ok().build();
        }
    }
}