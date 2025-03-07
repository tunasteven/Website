package com.example.demo;

import com.example.demo.model.entity.Product;
import com.example.demo.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<Product>> typeReference = new TypeReference<List<Product>>() {};
        InputStream inputStream = new ClassPathResource("男裝上身.json").getInputStream();
        List<Product> products = objectMapper.readValue(inputStream, typeReference);
        productRepository.saveAll(products);
        System.out.println("Product data loaded successfully.");
    }
}
