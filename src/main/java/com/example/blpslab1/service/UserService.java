package com.example.blpslab1.service;

import com.example.blpslab1.model.Session;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.SessionRepo;
import com.example.blpslab1.repo.StoredFileRepo;
import com.example.blpslab1.repo.UserRepo;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

import static com.example.blpslab1.service.ResponseStatus.*;
import static com.example.blpslab1.service.Role.USER;


@Service
public class UserService {
    public static double subscription_price = 1500;

    private final UserRepo userRepo;
    private final SessionRepo sessionRepo;
    private final StoredFileRepo storedFileRepo;
    private final MessageDigest md = MessageDigest.getInstance("SHA-512");

    public UserService(UserRepo userRepo, SessionRepo sessionRepo, StoredFileRepo storedFileRepo) throws NoSuchAlgorithmException {
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
        this.storedFileRepo = storedFileRepo;
    }


    public ResponseStatus signIn(User reqUser) {
        User realUser;
        try {
            realUser = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(reqUser.getUsername())).findFirst().get();
            String reqPass = encryptPassword(reqUser.getPassword());
            if (realUser.getPassword().equals(reqPass)) {
                Session session = getUserSession();
                if (session == null)
                    sessionRepo.save(new Session(USER, realUser.getUsername(), realUser.getSubscription()));
                else {
                    session.setUsername(realUser.getUsername());
                    session.setSubscription(realUser.getSubscription());
                    sessionRepo.save(session);
                }
                return GOOD;
            } else return WRONG_PASSWORD;
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

    public User getUser() {
        try {
            Session session = getUserSession();
            String username = session.getUsername();
            return userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
        } catch (NoSuchElementException | NullPointerException e) {
            return null;
        }
    }

    public ResponseStatus delete() {
        User user;
        try {
            Session session = getUserSession();
            String username = session.getUsername();
            user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
            userRepo.delete(user);
            if (session.getUsername().equals(username)) {
                session.setSubscription(null);
                session.setUsername(null);
                sessionRepo.save(session);
            }
            storedFileRepo.deleteAll(storedFileRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).toList());
            return GOOD;
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        }
    }


    public void exit() {
        try {
            Session session = getUserSession();
            session.setSubscription(null);
            session.setUsername(null);
            sessionRepo.save(session);
        } catch (NoSuchElementException e) {
            return;
        }
    }

    @Transactional
    public ResponseStatus buySub() {
        try {
            Session session = sessionRepo.findAll().stream().filter(session1 -> session1.getRole() == USER).findFirst().get();
            String username = session.getUsername();
            User user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();

            if (user.getSubscription()) {
                return ALREADY_EXISTS;
            } else {
                if (user.getWallet() < subscription_price) return OUT_OF_BALANCE;
                else {
                    user.setWallet(user.getWallet() - subscription_price);
                    user.setSubscription(true);
                    userRepo.save(user);
                    if (session.getUsername().equals(username)) {
                        session.setSubscription(true);
                        sessionRepo.save(session);
                    }
                }
                return GOOD;
            }
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        }
    }

    @Transactional
    public ResponseStatus cancelSub() {
        try {
            Session session = sessionRepo.findAll().stream().filter(session1 -> session1.getRole() == USER).findFirst().get();
            String username = session.getUsername();
            User user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();

            user.setSubscription(false);
            userRepo.save(user);
            if (session.getUsername().equals(username)) {
                session.setSubscription(false);
                sessionRepo.save(session);
            }
            return GOOD;
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        }
    }

    @Transactional
    public ResponseStatus putMoney(Double sum) {
        try {
            Session session = sessionRepo.findAll().stream().filter(session1 -> session1.getRole() == USER).findFirst().get();
            String username = session.getUsername();
            User user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
            user.setWallet(user.getWallet() + sum);
            userRepo.save(user);
            return GOOD;
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        }
    }


    @Transactional
    public ResponseStatus withdrawMoney(Double sum) {
        try {
            Session session = sessionRepo.findAll().stream().filter(session1 -> session1.getRole() == USER).findFirst().get();
            String username = session.getUsername();
            User user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
            user.setWallet(user.getWallet() - sum);
            userRepo.save(user);
            return GOOD;
        } catch (NoSuchElementException | NullPointerException e) {
            return NOT_FOUND;
        }
    }


    private String encryptPassword(final String password) {
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


    Session getUserSession() {
        try {
            return sessionRepo.findAll().stream().filter(session -> session.getRole() == USER).findFirst().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

}
