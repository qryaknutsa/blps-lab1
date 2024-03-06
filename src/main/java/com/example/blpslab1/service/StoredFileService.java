package com.example.blpslab1.service;

import com.example.blpslab1.model.Session;
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
import static com.example.blpslab1.service.Role.USER;
import static java.lang.Thread.sleep;

@Service
public class StoredFileService {
    private final StoredFileRepo storedFileRepo;
    private final SessionRepo sessionRepo;


    public StoredFileService(StoredFileRepo storedFileRepo, SessionRepo sessionRepo) {
        this.storedFileRepo = storedFileRepo;
        this.sessionRepo = sessionRepo;

    }

    public List<String> getAllFilesName() {
        try {
            String username = sessionRepo.findAll().stream().findFirst().get().getUsername();
            if (username.isEmpty()) return null;
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
            Session session = sessionRepo.findAll().stream().filter(user -> user.getRole() == USER).findFirst().get();
            String username = session.getUsername();
            try {
                Path path = Paths.get(filePath);
                String fileName = path.getFileName().toString();
                Binary data = new Binary(Files.readAllBytes(path));
                try {
                    storedFileRepo.findAll().stream().filter(file -> file.getUsername().equals(username)).filter(file -> file.getTitle().equals(fileName)).findFirst().get();
                    return ALREADY_EXISTS;
                } catch (NoSuchElementException e) {
                    StoredFile storedFile = new StoredFile(fileName, data, username);
                    storedFileRepo.save(storedFile);
                    if (!session.getSubscription()) sleep(5000);
                    return GOOD;
                }
            } catch (NoSuchElementException e) {
                return NOT_FOUND;
            }
        } catch (NoSuchElementException | InterruptedException e) {
            return NOT_SIGNED_IN;
        }
    }

    public StoredFile getStoredFile(String title) {
        try {
            String username = sessionRepo.findAll().stream().findFirst().get().getUsername();
            return storedFileRepo.findAll().stream().filter(file -> file.getUsername().equals(username)).filter(file -> file.getTitle().equals(title)).findFirst().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }


    public ResponseStatus deleteFile(String title) {
        try {
            String username = sessionRepo.findAll().stream().findFirst().get().getUsername();
            try {
                StoredFile storedFile = storedFileRepo.findAll().stream().filter(file -> file.getUsername().equals(username)).filter(file -> file.getTitle().equals(title)).findFirst().get();
                storedFileRepo.delete(storedFile);
                return GOOD;
            } catch (NoSuchElementException e) {
                return NOT_FOUND;
            }
        } catch (NoSuchElementException e) {
            return NOT_SIGNED_IN;
        }
    }
}
