package com.example.blpslab1.service;

import com.example.blpslab1.model.Session;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.SessionRepo;
import com.example.blpslab1.repo.UserRepo;


import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.NoSuchElementException;

import static com.example.blpslab1.service.ResponseStatus.*;

@Service
public class UserService {
    private final UserRepo userRepo;
    private final SessionRepo sessionRepo;
    private final MessageDigest md = MessageDigest.getInstance("SHA-512");

    public UserService(UserRepo userRepo, SessionRepo sessionRepo) throws NoSuchAlgorithmException {
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
    }

    public List<User> getAllUsers(){
        return userRepo.findAll();
    }



    public ResponseStatus signIn(User reqUser) {
        User realUser;
        try {
            realUser = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(reqUser.getUsername())).findFirst().get();
            String reqPass = encryptPassword(reqUser.getPassword());
            if (realUser.getPassword().equals(reqPass)) {
                sessionRepo.save(new Session(reqUser.getUsername()));
                return GOOD;
            }
            else return WRONG_PASSWORD;
        } catch (NoSuchElementException e) {
            return NOT_FOUND;
        }
    }


    public ResponseStatus signUp(User user) {
        try {
            userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(user.getUsername())).findFirst().get();
            return ALREADY_EXISTS;
        } catch (NoSuchElementException e) {
            user.setPassword(encryptPassword(user.getPassword()));
            userRepo.save(user);
            return GOOD;
        }
    }

    public User getUser(String username) {
        try{
            return userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
        } catch(NoSuchElementException e){
            return null;
        }
    }

    public ResponseStatus delete(String username){
        User user;
        try {
            user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
            userRepo.delete(user);
            return GOOD;
        } catch (NoSuchElementException e) {
            return NOT_FOUND;
        }
    }

    public ResponseStatus updateSub(String username) {
        try {
            User user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
            user.setSubscription(!(user.getSubscription()));
            userRepo.save(user);
            return GOOD;
        } catch (NoSuchElementException e) {
            return NOT_FOUND;
        }
    }


    private String encryptPassword(final String password){
        md.update(password.getBytes());
        byte[] byteBuffer = md.digest();
        StringBuilder strHexString = new StringBuilder();

        for (byte b : byteBuffer) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                strHexString.append('0');
            }
            strHexString.append(hex);
        }
        return strHexString.toString();
    }

}
