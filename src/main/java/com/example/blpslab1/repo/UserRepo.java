package com.example.blpslab1.repo;

import com.example.blpslab1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long>{
    User findById(long id);
}
