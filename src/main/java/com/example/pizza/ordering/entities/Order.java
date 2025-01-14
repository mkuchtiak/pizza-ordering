package com.example.pizza.ordering.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Date date;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties("orders")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    public Integer getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public Customer getCustomer() {
        return customer;
    }

    public BigDecimal getTotalPrice() {
        return orderItems.stream()
                .map(item -> item.getPizza().getPrice().multiply(BigDecimal.valueOf(item.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}