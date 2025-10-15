package com.example.miniwallet.controller;

import com.example.miniwallet.dto.*;
import com.example.miniwallet.entity.Customer;
import com.example.miniwallet.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDto> create(@RequestBody @Validated CustomerRequestDto req) {
        Customer toCreate = Customer.builder()
                .name(req.getName())
                .email(req.getEmail())
                .build();

        Customer saved = customerService.createCustomer(toCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> get(@PathVariable Long id) {
        Customer c = customerService.getById(id);
        return ResponseEntity.ok(toDto(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> update(@PathVariable Long id,
                                                      @RequestBody @Validated CustomerRequestDto req) {
        Customer incoming = Customer.builder()
                .name(req.getName())
                .email(req.getEmail())
                .build();
        Customer updated = customerService.updateCustomer(id, incoming);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }

    private CustomerResponseDto toDto(Customer c) {
        return CustomerResponseDto.builder()
                .id(c.getCustomerId())
                .name(c.getName())
                .email(c.getEmail())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
