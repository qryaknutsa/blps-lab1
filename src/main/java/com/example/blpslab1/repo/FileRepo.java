package com.example.blpslab1.repo;

import com.example.blpslab1.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepo extends JpaRepository<StoredFile, Long> {

    Optional<StoredFile> findStoredFileByTitleAndUsername(String title, String username);

    List<StoredFile> findAllByUsername(String username);

}
