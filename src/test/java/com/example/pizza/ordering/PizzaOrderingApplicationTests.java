package com.example.pizza.ordering;

import com.example.pizza.ordering.controllers.CustomerController;
import com.example.pizza.ordering.controllers.PizzaController;
import com.example.pizza.ordering.entities.Customer;
import com.example.pizza.ordering.entities.Order;
import com.example.pizza.ordering.entities.Pizza;
import com.example.pizza.ordering.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PizzaOrderingApplicationTests {
	private static final Integer FIRST_ORDER_ID = 1;

	String urlPrefix;
	Integer presidentId;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private PizzaController pizzaController;

	@Autowired
	private CustomerController customerController;
    @Autowired
    private OrderRepository orderRepository;

	@BeforeEach
	void beforeEach() {
		urlPrefix = "http://localhost:" + port + "/pizzaOrdering";

		pizzaController.addNewPizza("HW", "Hawai", BigDecimal.valueOf(129.99));
		pizzaController.addNewPizza("NP", "Napoli", BigDecimal.valueOf(150));
		pizzaController.addNewPizza("QF", "Quattro Formaggi", BigDecimal.valueOf(199));

		customerController.addNewCustomer("Donald Trump", "White House");

		presidentId = getEntityIdForName(
				"/customers",
				"Donald Trump",
				Customer[].class,
				Customer::getName,
				Customer::getId);
	}

	@AfterEach
	void afterEach() {
		pizzaController.removePizza("HW");
		pizzaController.removePizza("NP");
		pizzaController.removePizza("QF");;

		customerController.removeCustomer(presidentId);

		// check if pizzas, customers were removed
		checkEntities("/pizzas", Pizza[].class, Pizza::getName);
		checkEntities("/customers", Customer[].class, Customer::getName);
	}

	@Test
	void testGetPizzas() {
		checkEntities("/pizzas", Pizza[].class, Pizza::getName,
				"Hawai", "Napoli", "Quattro Formaggi");
	}

	@Test
	void testCreateAndRemovePizza() {
		MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
		formParameters.add("id", "CDF");
		formParameters.add("name", "Capo del Formaggio");
		formParameters.add("price", "200");

		addEntity("/pizzas", formParameters, "Pizza Capo del Formaggio created");
		checkEntities("/pizzas", Pizza[].class, Pizza::getName,
				"Hawai", "Napoli", "Quattro Formaggi", "Capo del Formaggio");

		restTemplate.delete(urlPrefix + "/pizzas/CDF");
		checkEntities("/pizzas", Pizza[].class, Pizza::getName,
				"Hawai", "Napoli", "Quattro Formaggi");
	}

	@Test
	void testGetCustomers() {
		checkEntities("/customers", Customer[].class, Customer::getName, "Donald Trump");
	}

	@Test
	void testCreateAndRemoveCustomer() {
		MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
		formParameters.add("name", "Petr Pavel");
		formParameters.add("address", "Prazsky Hrad");

		addEntity("/customers", formParameters, "Customer Petr Pavel created");

		checkEntities("/customers", Customer[].class, Customer::getName,
				"Donald Trump", "Petr Pavel");

		Integer customerId = getEntityIdForName(
				"/customers",
				"Petr Pavel",
				Customer[].class,
				Customer::getName,
				Customer::getId);

		restTemplate.delete(urlPrefix + "/customers/" + customerId);

		checkEntities("/customers", Customer[].class, Customer::getName,
				"Donald Trump");
	}

	@Test
	void testCreateAndRemoveOrder() {
		MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
		formParameters.add("pizzaList", "HW,QF:2");

		addEntity("/customers/" + presidentId + "/addOrder",
				formParameters,
				"Order " + FIRST_ORDER_ID + " created for Donald Trump customer");

		ResponseEntity<Order> response = restTemplate.getForEntity(
				urlPrefix + "/orders/" + FIRST_ORDER_ID, Order.class);

		Order resp = response.getBody();

		assertThat(resp).isNotNull();
        assertThat(resp.getOrderItems().size()).isEqualTo(2);
		assertThat(resp.getTotalPrice().toString()).isEqualTo("527.99");
		assertThat(resp.getCustomerName()).isEqualTo("Donald Trump");

		restTemplate.delete(urlPrefix + "/orders/" + FIRST_ORDER_ID);
		checkEntities("/orders", Order[].class, Order::getCustomerName);
	}

	private <E> void checkEntities(String uri,
								   Class<E[]> collectionClazz,
								   Function<E, String> nameFunction,
								   String... expectedNames) {
		ResponseEntity<E[]> response = restTemplate.getForEntity(urlPrefix + uri, collectionClazz);

		assertThat(response.getStatusCode().value()).isEqualTo(200);

		List<E> allEntities = Arrays.asList(Objects.requireNonNull(response.getBody()));
		assertThat(allEntities.size()).isEqualTo(expectedNames.length);

		List<String> entityNames = allEntities.stream().map(nameFunction).collect(Collectors.toList());
		assertThat(entityNames).containsExactlyInAnyOrder(expectedNames);
	}

	private <E, R> R getEntityIdForName(String uri,
										String name,
										Class<E[]> collectionClazz,
										Function<E, String> nameFunction,
										Function<E, R> idFunction) {
		ResponseEntity<E[]> response = restTemplate.getForEntity(urlPrefix + uri, collectionClazz);

		List<E> allEntities = Arrays.asList(Objects.requireNonNull(response.getBody()));

		Optional<E> opt = allEntities.stream().filter(e -> nameFunction.apply(e).equals(name)).findFirst();
		assertThat(opt.isPresent()).isTrue();
		return idFunction.apply(opt.get());
	}

	private void addEntity(String uri, MultiValueMap<String, String> formParameters, String expectedResponse) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formParameters, headers);

		ResponseEntity<String> resp = restTemplate.postForEntity(urlPrefix + uri, request, String.class);

		assertThat(resp.getBody()).isEqualTo(expectedResponse);
	}

}
