package com.example.blpslab1.repo;

import com.example.blpslab1.model.Ownership;
import com.example.blpslab1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;

public interface OwnershipRepo extends JpaRepository<Ownership, Long> {
    Optional<Ownership> findOwnershipByUserLoginAndFileId(String userLogin, String fileId);


    Collection<Ownership> findAllByUserLogin(String userLogin);
}
