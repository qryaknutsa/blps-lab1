package com.example.blpslab1.repo;

import com.example.blpslab1.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepo extends MongoRepository<Session, String> {

}
