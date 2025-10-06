package com.waturnos.service;

import com.waturnos.entity.Customer;
import com.waturnos.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    public List<Customer> findByTenant(Long tenantId) {
        return repo.findByTenantTenantId(tenantId);
    }

    public Customer save(Customer customer) {
        return repo.save(customer);
    }
}