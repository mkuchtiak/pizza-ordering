package com.example.pizza.ordering.controllers;

import com.example.pizza.ordering.repositories.OrderRepository;
import com.example.pizza.ordering.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Controller
@RequestMapping(path=Constants.RESOURCE_URI_PREFIX + "/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping(path="")
    public @ResponseBody Iterable<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping(path="{orderId}")
    public @ResponseBody Order getOrder(@PathVariable("orderId") int orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path="{orderId}")
    public ResponseEntity<?> removeOrder(@PathVariable("orderId") int orderId) {
        return Utils.removeEntity(orderRepository, orderId);
    }
}
