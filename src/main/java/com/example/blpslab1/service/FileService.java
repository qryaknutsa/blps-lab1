package com.example.blpslab1.service;

import com.example.blpslab1.exceptions.*;
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
import java.util.stream.Collectors;

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


    //UserNotFoundException
    public List<String> getAllFilesNameByUsername(String username) {
        userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        return fileRepo.findAllByUsername(username).stream()
                .map(StoredFile::getTitle)
                .collect(Collectors.toList());

    }

    //FileAlreadyExistsException
    //UserNotFoundException
    public void upload(String username, String filePath) throws IOException, InterruptedException {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        Binary data = new Binary(Files.readAllBytes(path));
        try {
            fileRepo.findStoredFileByTitleAndUsername(fileName, username).orElseThrow(FileNotFoundException::new);
            throw new FileAlreadyExistsException("Файл с таким именем уже существует");
        } catch (FileNotFoundException e) {
            StoredFile storedFile = new StoredFile(fileName, data, username);
            fileRepo.save(storedFile);
            if (!user.getSubscription()) sleep(5000);
        }


    }


    //FileAlreadyExistException
    //UserNotFoundException
    public StoredFile getStoredFile(String username, String title) {
        userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        return fileRepo.findStoredFileByTitleAndUsername(title, username).orElseThrow(FileNotFoundException::new);

    }

    //FileAlreadyExistException
    //UserNotFoundException
    public void deleteFile(String username, String title) {
        userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        StoredFile storedFile = fileRepo.findStoredFileByTitleAndUsername(title, username).orElseThrow(FileNotFoundException::new);
        fileRepo.delete(storedFile);

    }


}
