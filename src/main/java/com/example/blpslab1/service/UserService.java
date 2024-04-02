package com.example.blpslab1.service;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.BitronixTransactionManager;
import com.example.blpslab1.dto.ResponseStatus;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.FileRepo;
import com.example.blpslab1.repo.UserRepo;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.transaction.*;
import java.util.List;
import java.util.NoSuchElementException;

import static com.example.blpslab1.dto.ResponseStatus.*;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    public static double subscription_price = 1500;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final FileRepo fileRepo;


    public List<User> getAllUsers() {
        try {
            return userRepo.findAll();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public User getUser(String username) {
        try {
            return userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
        } catch (NoSuchElementException | NullPointerException e) {
            return null;
        }
    }

    public ResponseStatus saveUser(User user){
        try{
            userRepo.findUserByUsername(user.getUsername()).orElseThrow(NoSuchElementException::new);
            return ALREADY_EXISTS;
        } catch (NoSuchElementException e){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepo.save(user);
            return GOOD;
        }
    }


    public ResponseStatus delete(String username) {
        User user;
        try {
            user = userRepo.findUserByUsername(username).orElseThrow(NoSuchElementException::new);
            userRepo.delete(user);
            fileRepo.deleteAll(fileRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).toList());
            return GOOD;
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        }
    }

    public ResponseStatus changePassword(User user) {
        try {
            User user1 = userRepo.findUserByUsername(user.getUsername()).orElseThrow(NoSuchElementException::new);
            user1.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepo.save(user1);
            return GOOD;
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        }
    }

    public ResponseStatus changeUsername(String oldUsername, User user) {
        try {
            userRepo.findUserByUsername(user.getUsername()).orElseThrow(NoSuchElementException::new);
            return ALREADY_EXISTS;
        } catch (NoSuchElementException e) {
            User user1 = userRepo.findUserByUsername(oldUsername).orElseThrow();
            user1.setUsername(user.getUsername());
            userRepo.save(user1);
            return GOOD;
        }
    }


    public ResponseStatus buySub(String username) throws SystemException {
        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();

        try {
            User user = userRepo.findUserByUsername(username).orElseThrow(NoSuchElementException::new);
            if (user.getSubscription()) {
                return ALREADY_EXISTS;
            } else {
                if (user.getWallet() < subscription_price) return OUT_OF_BALANCE;
                else {
                    tm.begin();
                    user.setWallet(user.getWallet() - subscription_price);
                    user.setSubscription(true);
                    userRepo.save(user);
                    tm.commit();
                }
                return GOOD;
            }
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        } catch (Exception e) {
            tm.rollback();
        }
        return NOT_FOUND;
    }


    public ResponseStatus cancelSub(String username) throws SystemException {
        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();
        try {
            User user = userRepo.findUserByUsername(username).orElseThrow(NoSuchElementException::new);
            tm.begin();
            user.setSubscription(false);
            userRepo.save(user);
            tm.commit();
            return GOOD;
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        } catch (Exception e) {
            tm.rollback();
        }
        return NOT_FOUND;
    }

    public ResponseStatus putMoney(String username, Double sum) throws SystemException  {
        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();
        try {
            User user = userRepo.findUserByUsername(username).orElseThrow(NoSuchElementException::new);
            tm.begin();
            user.setWallet(user.getWallet() + sum);
            userRepo.save(user);
            tm.commit();
            return GOOD;
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        } catch (Exception e) {
            tm.rollback();
        }
        return NOT_FOUND;
    }

    public ResponseStatus withdrawMoney(String username, Double sum) throws SystemException {
        BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();
        try {
            User user = userRepo.findUserByUsername(username).orElseThrow(NoSuchElementException::new);
            tm.begin();
            user.setWallet(user.getWallet() - sum);
            userRepo.save(user);
            tm.commit();
            return GOOD;
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        } catch (Exception e) {
            tm.rollback();
        }
        return NOT_FOUND;

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return userRepo.findUserByUsername(username).orElseThrow(NoSuchElementException::new);
        } catch (NoSuchElementException e) {
            throw new UsernameNotFoundException("User not found");
        }
    }
}
