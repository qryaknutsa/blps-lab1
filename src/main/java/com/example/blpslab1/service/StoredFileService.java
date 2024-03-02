package com.example.blpslab1.service;

import com.example.blpslab1.model.StoredFile;
import com.example.blpslab1.repo.SessionRepo;
import com.example.blpslab1.repo.StoredFileRepo;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;

import static com.example.blpslab1.service.ResponseStatus.*;

@Service
public class StoredFileService {
    private final StoredFileRepo storedFileRepo;
    private final SessionRepo sessionRepo;


    public StoredFileService(StoredFileRepo storedFileRepo, SessionRepo sessionRepo) {
        this.storedFileRepo = storedFileRepo;
        this.sessionRepo = sessionRepo;

    }

    //TODO: запретить пытаться получить данные пока не авторизовался
    public List<String> getAllFilesName() {
        try {
            String username = sessionRepo.findAll().stream().findFirst().get().getUsername();
            return storedFileRepo
                    .findAll()
                    .stream()
                    .filter(file -> file.getUsername().equals(username))
                    .toList()
                    .stream()
                    .map(StoredFile::getTitle)
                    .toList();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public ResponseStatus upload(String filePath) throws IOException {
        try {
            String username = sessionRepo.findAll().stream().findFirst().get().getUsername();
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();
            Binary data = new Binary(Files.readAllBytes(path));
            try {
                storedFileRepo.findAll().stream().filter(file -> file.getTitle().equals(fileName)).findFirst().get();
                return ALREADY_EXISTS;
            } catch (NoSuchElementException e) {
                StoredFile storedFile = new StoredFile(fileName, data, username);
                storedFileRepo.save(storedFile);
                return GOOD;
            }
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    //TODO: здесь нет разницы между "нет файла" и "не авторизован"
    public StoredFile getStoredFile(String title) {
        try {
            sessionRepo.findAll().stream().findFirst().get();
            return storedFileRepo.findAll().stream().filter(file -> file.getTitle().equals(title)).findFirst().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }


//    public ResponseStatus delete(String username) {
//        User user;
//        try {
//            user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
//            userRepo.delete(user);
//            return GOOD;
//        } catch (NoSuchElementException e) {
//            return NOT_FOUND;
//        }
//    }
//
//
//    public ResponseStatus updateSub(String username) {
//        try {
//            User user = userRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(username)).findFirst().get();
//            user.setSubscription(!(user.getSubscription()));
//            userRepo.save(user);
//            return GOOD;
//        } catch (NoSuchElementException e) {
//            return NOT_FOUND;
//        }
//    }

}
