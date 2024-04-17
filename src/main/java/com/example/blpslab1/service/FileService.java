package com.example.blpslab1.service;

import com.example.blpslab1.exceptions.*;
import com.example.blpslab1.model.StoredFile;
import com.example.blpslab1.repo.FileRepo;
import com.example.blpslab1.serviceConnection.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@Slf4j
@Service
@Component
@EnableJms
public class FileService {
    private final JmsTemplate jmsTemplate;
    private final FileRepo fileRepo;
    private final String queueName = "File";

    @Autowired
    public FileService(FileRepo fileRepo, JmsTemplate jmsTemplate) {
        this.fileRepo = fileRepo;
        this.jmsTemplate = jmsTemplate;
    }


    @JmsListener(destination = queueName + "AllFilesNames-request")
    public void getAllFilesName(Message message) {
        List<String> list = fileRepo.findAll().stream()
                .map(file -> "Title: " + file.getTitle() + ", username: " + file.getUsername())
                .toList();
        Message response = new Message(list);
        jmsTemplate.convertAndSend(queueName + "AllFilesNames-response", response);
    }


    //UserNotFoundException
    @JmsListener(destination = queueName + "AllFilesNamesByUsername-request")
    public void getAllFilesNameByUsername(Message message) {
        List<String> list = fileRepo.findAllByUsername(message.getUsername()).stream()
                .map(StoredFile::getTitle)
                .toList();
        Message response = new Message(list);
        jmsTemplate.convertAndSend(queueName + "AllFilesNamesByUsername-response", response);
    }

    //FileAlreadyExistsException
    //UserNotFoundException
    @JmsListener(destination = queueName + "SaveFile-request")
    public void upload(Message message) throws IOException, InterruptedException {
        Path path = Paths.get(message.getFilePath());
        String fileName = path.getFileName().toString();
        String data = Files.readString(path);
        try {
            fileRepo.findStoredFileByTitleAndUsername(fileName, message.getUsername()).orElseThrow(FileNotFoundException::new);
            if (!message.isSubscription()) sleep(5000);
            jmsTemplate.convertAndSend(queueName + "SaveFile-response", new Message("Файл " + fileName + " не найден"));
        } catch (FileNotFoundException e) {
            StoredFile storedFile = new StoredFile(fileName, data, message.getUsername());
            if (!message.isSubscription()) sleep(5000);
            fileRepo.save(storedFile);
            jmsTemplate.convertAndSend(queueName + "SaveFile-response", new Message("Файл " + fileName + " сохранен"));
        }


    }


    //FileAlreadyExistException
    //UserNotFoundException
    @JmsListener(destination = queueName + "GetFile-request")
    public void getStoredFile(Message message) {
        StoredFile file = fileRepo.findStoredFileByTitleAndUsername(message.getTitle(), message.getUsername()).orElseThrow(FileNotFoundException::new);
        Message response = new Message(file.getTitle(), file.getData(), file.getUsername());
        jmsTemplate.convertAndSend(queueName + "GetFile-response", response);
    }

    //FileAlreadyExistException
    //UserNotFoundException
    @JmsListener(destination = queueName + "DeleteFile-request")
    public void deleteFile(Message message) {
        try {
            StoredFile storedFile = fileRepo.findStoredFileByTitleAndUsername(message.getTitle(), message.getUsername()).orElseThrow(FileNotFoundException::new);
            fileRepo.delete(storedFile);
            jmsTemplate.convertAndSend(queueName + "DeleteFile-response", new Message("Файл " + message.getTitle() + " удалён"));
        }catch (FileNotFoundException e){
            jmsTemplate.convertAndSend(queueName + "DeleteFile-response", new Message("Файл " + message.getTitle() + " не найден"));
        }
    }

    @JmsListener(destination = queueName + "DeleteAllFilesByUsername-request")
    public void deleteAllFilesByUsername(Message message) {
        fileRepo.deleteAll(fileRepo.findAll().stream().filter(the_user -> the_user.getUsername().equals(message.getUsername())).toList());
        jmsTemplate.convertAndSend(queueName + "DeleteAllFilesByUsername-response", new Message("Все файлы пользователя " + message.getUsername() + " удалены"));
    }

}
