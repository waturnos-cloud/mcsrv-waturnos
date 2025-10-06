package com.waturnos.controller;

import com.waturnos.entity.Customer;
import com.waturnos.service.CustomerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/tenant/{tenantId}")
    public List<Customer> getByTenant(@PathVariable Long tenantId) {
        return customerService.findByTenant(tenantId);
    }

    @PostMapping
    public Customer create(@RequestBody Customer customer) {
        return customerService.save(customer);
    }
}