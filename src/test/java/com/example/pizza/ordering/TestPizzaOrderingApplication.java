package com.example.pizza.ordering;

import org.springframework.boot.SpringApplication;

public class TestPizzaOrderingApplication {

	public static void main(String[] args) {
		SpringApplication.from(PizzaOrderingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
