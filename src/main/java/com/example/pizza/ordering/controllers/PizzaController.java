package com.example.pizza.ordering.controllers;

import com.example.pizza.ordering.repositories.PizzaRepository;
import com.example.pizza.ordering.entities.Pizza;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URI;

import static com.example.pizza.ordering.controllers.Exceptions.AlreadyExistsException;

@Controller
@RequestMapping(path=Constants.RESOURCE_URI_PREFIX + "/pizzas")
public class PizzaController {

    @Autowired
    private PizzaRepository pizzaRepository;

    @GetMapping(path="")
    public @ResponseBody Iterable<Pizza> getAllPizzas() {
        return pizzaRepository.findAll();
    }

    @PostMapping(path="")
    public ResponseEntity<?> addNewPizza(@RequestParam String id,
                                         @RequestParam String name,
                                         @RequestParam BigDecimal price) {

        if (pizzaRepository.findById(id).isPresent()) {
            throw new AlreadyExistsException("Pizza with id " + id + " already exists");
        }

        Pizza p = new Pizza();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        pizzaRepository.save(p);

        return ResponseEntity.created(URI.create(Constants.RESOURCE_URI_PREFIX + "/pizzas/" + id))
                .body(String.format("Pizza %s created", p.getName()));

    }

    @GetMapping(path="{pizzaId}")
    public @ResponseBody Pizza getPizza(@PathVariable("pizzaId") String pizzaId) {
        return pizzaRepository.findById(pizzaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path="{pizzaId}")
    public @ResponseBody ResponseEntity<?> removePizza(@PathVariable("pizzaId") String pizzaId) {
        return Utils.removeEntity(pizzaRepository, pizzaId);
    }
}
