package com.example.blpslab1.repo;

import com.example.blpslab1.model.StoredFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StoredFileRepo extends MongoRepository<StoredFile, String> {

}
