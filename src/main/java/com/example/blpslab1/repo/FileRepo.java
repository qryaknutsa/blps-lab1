package com.example.blpslab1.repo;

import com.example.blpslab1.model.StoredFile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepo extends MongoRepository<StoredFile, String> {

    Optional<StoredFile> findStoredFileByTitleAndUsername(String title, String username);

    List<StoredFile> findAllByUsername(String username);

}
