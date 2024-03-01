package com.example.blpslab1.repo;

import com.example.blpslab1.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepo extends MongoRepository<User, String> {


}
