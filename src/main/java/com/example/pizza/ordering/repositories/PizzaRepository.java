package com.example.pizza.ordering.repositories;

import com.example.pizza.ordering.entities.Pizza;
import org.springframework.data.repository.CrudRepository;

public interface PizzaRepository extends CrudRepository<Pizza, String> {
}
