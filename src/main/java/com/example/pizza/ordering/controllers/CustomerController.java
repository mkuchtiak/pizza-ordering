package com.example.pizza.ordering.controllers;

import com.example.pizza.ordering.repositories.CustomerRepository;
import com.example.pizza.ordering.repositories.OrderItemRepository;
import com.example.pizza.ordering.repositories.OrderRepository;
import com.example.pizza.ordering.repositories.PizzaRepository;
import com.example.pizza.ordering.entities.Customer;
import com.example.pizza.ordering.entities.Order;
import com.example.pizza.ordering.entities.OrderItem;
import com.example.pizza.ordering.entities.Pizza;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(path=Constants.RESOURCE_URI_PREFIX + "/customers")
public class CustomerController {

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @GetMapping(path="")
    public @ResponseBody Iterable<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @PostMapping(path="")
    public @ResponseBody String addNewCustomer(@RequestParam String name,
                                               @RequestParam String address) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setAddress(address);
        customerRepository.save(customer);
        return String.format("Customer %s created", name);
    }

    @GetMapping(path="{customerId}")
    public @ResponseBody Customer getCustomers(@PathVariable("customerId") int customerId) {
        return customerRepository.findById(customerId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path="{customerId}")
    public ResponseEntity<?> removeCustomer(@PathVariable("customerId") Integer customerId) {
        return Utils.removeEntity(customerRepository, customerId);
    }

    @PostMapping(path="{customerId}/addOrder")
    public @ResponseBody String addOrder(@PathVariable("customerId") int customerId,
                                         @RequestParam String pizzaList) {
        Order order = new Order();
        order.setDate(new Date());
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer id")
        );
        order.setCustomer(customer);

        List<OrderItem> items = getPizzaInfosFromQueryParameter(pizzaList).stream()
                .map(pizzaInfo -> {
                    if (pizzaInfo.amount() < 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pizzaList");
                    }
                    Pizza pizza = pizzaRepository.findById(pizzaInfo.pizzaId()).orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    String.format("Invalid pizza ID; %s", pizzaInfo.pizzaId()))
                    );
                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setAmount(pizzaInfo.amount());
                    item.setPizza(pizza);
                    return item;
                }).toList();

        order.setOrderItems(items);
        orderRepository.save(order);

        return String.format("Order %d created for %s customer", order.getId(), customer.getName());
    }

    private List<PizzaInfo> getPizzaInfosFromQueryParameter(String pizzaList) {
        return Arrays.stream(pizzaList.split(","))
                .map(pizza -> {
                    if (!pizza.contains(":")) {
                        return new PizzaInfo(pizza, 1);
                    } else {
                        String[] pizzaIndexAndCount = pizza.split(":");
                        if (pizzaIndexAndCount.length == 2) {
                            return new PizzaInfo(pizzaIndexAndCount[0], Integer.parseInt(pizzaIndexAndCount[1]));
                        } else {
                            return (new PizzaInfo("", -1));
                        }
                    }
                }).toList();
    };

    private record PizzaInfo(String pizzaId, int amount) {}

}
