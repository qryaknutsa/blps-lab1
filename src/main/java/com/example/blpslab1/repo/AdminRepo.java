package com.example.blpslab1.repo;

import com.example.blpslab1.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminRepo extends MongoRepository<Admin, String> {
}
