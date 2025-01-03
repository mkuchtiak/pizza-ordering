package com.example.pizza.ordering.repositories;

import com.example.pizza.ordering.entities.OrderItem;
import org.springframework.data.repository.CrudRepository;

public interface OrderItemRepository extends CrudRepository<OrderItem, Integer> {
}
