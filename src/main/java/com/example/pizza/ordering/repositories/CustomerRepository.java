package com.example.pizza.ordering.repositories;

import com.example.pizza.ordering.entities.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Integer> {
}
