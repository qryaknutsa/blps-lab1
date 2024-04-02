package com.example.blpslab1.service;

import com.example.blpslab1.dto.ResponseStatus;
import com.example.blpslab1.model.StoredFile;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.FileRepo;
import com.example.blpslab1.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.example.blpslab1.dto.ResponseStatus.*;
import static java.lang.Thread.sleep;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepo fileRepo;
    private final UserRepo userRepo;


    public List<String> getAllFilesName() {
        return fileRepo.findAll().stream()
                .map(StoredFile::getTitle)
                .collect(Collectors.toList());
    }

    public List<String> getAllFilesName(String username) {
        return fileRepo.findAllByUsername(username).stream()
                .map(StoredFile::getTitle)
                .collect(Collectors.toList());

    }

    public ResponseStatus upload(String username, String filePath) throws IOException {
        try {
            User user = userRepo.findUserByUsername(username).orElseThrow(NoSuchElementException::new);;
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();
            Binary data = new Binary(Files.readAllBytes(path));
            try {
                fileRepo.findStoredFileByTitleAndUsername(fileName, username).orElseThrow(NoSuchElementException::new);
                return ALREADY_EXISTS;
            } catch (NoSuchElementException e) {
                StoredFile storedFile = new StoredFile(fileName, data, username);
                fileRepo.save(storedFile);
                if (!user.getSubscription()) sleep(5000);
                return GOOD;
            }
        } catch (NoSuchElementException | InterruptedException e) {
            return NOT_FOUND;
        }
    }

    public StoredFile getStoredFile(String username, String title) {
        try {
            return fileRepo.findStoredFileByTitleAndUsername(title, username).orElseThrow(NoSuchElementException::new);
        } catch (NoSuchElementException e) {
            return null;
        }
    }


    public ResponseStatus deleteFile(String username, String title) {
        try {
            StoredFile storedFile = fileRepo.findStoredFileByTitleAndUsername(title, username).orElseThrow(NoSuchElementException::new);
            fileRepo.delete(storedFile);
            return GOOD;
        } catch (NoSuchElementException e) {
            return NOT_FOUND;
        }

    }
}
