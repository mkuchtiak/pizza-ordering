package com.example.pizza.ordering.repositories;

import com.example.pizza.ordering.entities.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Integer> {
}
