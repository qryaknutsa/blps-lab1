package com.example.blpslab1.service;

import com.example.blpslab1.model.Admin;
import com.example.blpslab1.model.Session;
import com.example.blpslab1.model.StoredFile;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.AdminRepo;
import com.example.blpslab1.repo.SessionRepo;
import com.example.blpslab1.repo.StoredFileRepo;
import com.example.blpslab1.repo.UserRepo;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.NoSuchElementException;

import static com.example.blpslab1.service.ResponseStatus.*;
import static com.example.blpslab1.service.Role.ADMIN;
import static com.example.blpslab1.service.Role.USER;
import static java.lang.Thread.sleep;

@Service
public class AdminService {
    private final UserRepo userRepo;
    private final SessionRepo sessionRepo;
    private final StoredFileRepo storedFileRepo;
    private final AdminRepo adminRepo;
    private final MessageDigest md = MessageDigest.getInstance("SHA-512");

    public AdminService(UserRepo userRepo, SessionRepo sessionRepo, StoredFileRepo storedFileRepo, AdminRepo adminRepo) throws NoSuchAlgorithmException {
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
        this.storedFileRepo = storedFileRepo;
        this.adminRepo = adminRepo;
    }

    public List<User> getAllUsers() {
        try {
            if (checkAdminSession()) return userRepo.findAll();
            else return null;
        } catch (NoSuchElementException e) {
            return null;
        }
    }


    public ResponseStatus signIn(Admin reqAdmin) {
        Admin realAdmin;
        try {
            realAdmin = adminRepo.findAll().stream().filter(admin1 -> admin1.getUsername().equals(reqAdmin.getUsername())).findFirst().get();
            String reqPass = encryptPassword(reqAdmin.getPassword());
            if (realAdmin.getPassword().equals(reqPass)) {
                Session session = getAdminSession();
                if (session == null) sessionRepo.save(new Session(ADMIN, realAdmin.getUsername()));
                else {
                    session.setUsername(realAdmin.getUsername());
                    sessionRepo.save(session);
                }
                return GOOD;
            } else return WRONG_PASSWORD;
        } catch (NoSuchElementException e) {
            return NOT_FOUND;
        }
    }


    public ResponseStatus createUser(User user) {
        if (checkAdminSession()) {
            try {
                userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(user.getUsername())).findFirst().get();
                return ALREADY_EXISTS;
            } catch (NoSuchElementException e) {
                user.setPassword(encryptPassword(user.getPassword()));
                userRepo.save(user);
                return GOOD;
            }
        } else return NOT_SIGNED_IN;
    }

    public ResponseStatus createAdmin(Admin admin) {
        if (!adminRepo.findAll().stream().toList().isEmpty()) {
            if (checkAdminSession()) {
                try {
                    adminRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(admin.getUsername())).findFirst().get();
                    return ALREADY_EXISTS;
                } catch (NoSuchElementException e) {
                    admin.setPassword(encryptPassword(admin.getPassword()));
                    adminRepo.save(admin);
                    return GOOD;
                }
            } else return NOT_SIGNED_IN;
        } else {
            admin.setPassword(encryptPassword(admin.getPassword()));
            adminRepo.save(admin);
            return GOOD;
        }
    }

    public User getUser(String username) {
        boolean flag = checkAdminSession();
        if (flag) {
            try {
                return userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
            } catch (NoSuchElementException e) {
                return null;
            }
        } else return null;
    }

    public Admin getAdmin(String username) {
        if (checkAdminSession()) {
            try {
                return adminRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
            } catch (NoSuchElementException e) {
                return null;
            }
        } else return null;
    }

    public ResponseStatus deleteUser(String username) {
        if (checkAdminSession()) {
            User user;
            try {
                user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
                userRepo.delete(user);
                try {
                    Session session = sessionRepo.findAll().stream().findFirst().get();
                    if (session.getUsername().equals(username))
                        sessionRepo.delete(sessionRepo.findAll().stream().findFirst().get());
                    storedFileRepo.deleteAll(storedFileRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).toList());
                } catch (NoSuchElementException e) {
                    return GOOD;
                }
                return GOOD;
            } catch (NoSuchElementException e) {
                return NOT_FOUND;
            }
        } else return NOT_SIGNED_IN;
    }

    public ResponseStatus deleteAdmin(String username) {
        if (checkAdminSession()) {
            Admin admin;
            try {
                admin = adminRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
                adminRepo.delete(admin);
                return GOOD;
            } catch (NoSuchElementException e) {
                return NOT_FOUND;
            }
        } else return NOT_SIGNED_IN;
    }

//    public ResponseStatus upload(String filePath) throws IOException {
//        if (checkAdminSession()) {
////            Session session = sessionRepo.findAll().stream().findFirst().get();
////            String username = session.getUsername();
////            try {
////                Path path = Paths.get(filePath);
////                String fileName = path.getFileName().toString();
////                Binary data = new Binary(Files.readAllBytes(path));
////                try {
////                    storedFileRepo.findAll().stream().filter(file -> file.getUsername().equals(username)).filter(file -> file.getTitle().equals(fileName)).findFirst().get();
////                    return ALREADY_EXISTS;
////                } catch (NoSuchElementException e) {
////                    StoredFile storedFile = new StoredFile(fileName, data, username);
////                    storedFileRepo.save(storedFile);
////                    if (!session.getSubscription()) sleep(5000);
////                    return GOOD;
////                }
////            } catch (NoSuchElementException | InterruptedException e) {
////                return NOT_FOUND;
////            }
//        } else return NOT_SIGNED_IN;
//    }

//    public ResponseStatus updateSub(String username) {
//        try {
//            User user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
//            user.setSubscription(!(user.getSubscription()));
//            userRepo.save(user);
//            Session session = sessionRepo.findAll().stream().findFirst().get();
//            if (session.getUsername().equals(username)) {
//                session.setSubscription(user.getSubscription());
//                sessionRepo.save(session);
//            }
//            return GOOD;
//        } catch (NoSuchElementException e) {
//            return NOT_FOUND;
//        }
//    }

    public void exit() {
        try {
            Session session = sessionRepo.findAll().stream().filter(session1 -> session1.getRole() == ADMIN).findFirst().get();
            session.setUsername(null);
            sessionRepo.save(session);
        } catch (NoSuchElementException e) {
            return;
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


    boolean checkAdminSession() {
        try {
            String s = sessionRepo.findAll().stream().filter(session -> session.getRole() == ADMIN).findFirst().get().getUsername();
            return !(s == null);
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    Session getAdminSession(){
        try {
            return sessionRepo.findAll().stream().filter(session -> session.getRole() == ADMIN).findFirst().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
