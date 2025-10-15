// package com.example.miniwallet.service;
package com.example.miniwallet.service;

import com.example.miniwallet.entity.Customer;
import com.example.miniwallet.exception.DuplicateResourceException;
import com.example.miniwallet.exception.ResourceNotFoundException;
import com.example.miniwallet.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new DuplicateResourceException("customer with email already exists: " + customer.getEmail());
        }
        // ensure id not set
        customer.setCustomerId(null);
        return customerRepository.save(customer);
    }

    @Cacheable(value = "customers", key = "'customer_' + #id")
    public Customer getById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Cacheable(value = "customers", key = "'customer_email_' + #email")
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Transactional
    @CachePut(value = "customers", key = "'customer_' + #id")
    public Customer updateCustomer(Long id, Customer incoming) {
        Customer existing = getById(id);
        if (incoming.getName() != null) existing.setName(incoming.getName());
        if (incoming.getEmail() != null && !incoming.getEmail().equals(existing.getEmail())) {
            if (customerRepository.existsByEmail(incoming.getEmail())) {
                throw new DuplicateResourceException("email already used: " + incoming.getEmail());
            }
            existing.setEmail(incoming.getEmail());
        }
        return customerRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "customers", key = "'customer_' + #id")
    public void deleteCustomer(Long id) {
        Customer c = getById(id);
        customerRepository.delete(c);
    }
}
