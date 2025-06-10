package com.example.Lab_OOP.repo;

import com.example.Lab_OOP.models.Flower;
import org.springframework.data.repository.CrudRepository;

public interface FlowerRepository extends CrudRepository<Flower, Integer> {
}
