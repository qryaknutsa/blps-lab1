package com.example.blpslab1.service;

import com.example.blpslab1.exceptions.*;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.UserRepo;
import com.example.blpslab1.serviceConnection.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Slf4j
@Service
@Component
@EnableJms
public class FileService {
    private final JmsTemplate jmsTemplate;
    private final UserRepo userRepo;

    private final String queueName = "File";

    @Autowired
    public FileService(UserRepo userRepo, JmsTemplate jmsTemplate) {
        this.userRepo = userRepo;
        this.jmsTemplate = jmsTemplate;
    }

    public List<String> getAllFilesName() {
        Message request = new Message();
        jmsTemplate.convertAndSend(queueName + "AllFilesNames-request", request);
        Message response = (Message) jmsTemplate.receiveAndConvert(queueName + "AllFilesNames-response");
        assert response != null;
        return response.getList();
    }


    //UserNotFoundException
    public List<String> getAllFilesNameByUsername(String username) {
        userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        Message request = new Message(username);
        jmsTemplate.convertAndSend(queueName + "AllFilesNamesByUsername-request", request);
        Message response = (Message) jmsTemplate.receiveAndConvert(queueName + "AllFilesNamesByUsername-response");
        assert response != null;
        return response.getList();
    }

    //FileAlreadyExistsException
    //UserNotFoundException
    public void upload(String username, String filePath) throws IOException, InterruptedException {
        User user = userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        Message request = new Message(username, filePath, user.getSubscription());
        jmsTemplate.convertAndSend(queueName + "SaveFile-request", request);

    }


    //FileAlreadyExistException
    //UserNotFoundException
    public Message getStoredFile(String username, String title) {
        userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        Message request = new Message(title, username);
        jmsTemplate.convertAndSend(queueName + "GetFile-request", request);
        return (Message) jmsTemplate.receiveAndConvert(queueName + "GetFile-response");
//        return fileRepo.findStoredFileByTitleAndUsername(title, username).orElseThrow(FileNotFoundException::new);

    }

    //FileAlreadyExistException
    //UserNotFoundException
    public void deleteFile(String username, String title) {
        userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        Message request = new Message(title, username);
        jmsTemplate.convertAndSend(queueName + "DeleteFile-request", request);
    }

    public void deleteAllFilesByUsername(String username) {
        userRepo.findUserByUsername(username).orElseThrow(UserNotFoundException::new);
        Message request = new Message(username);
        jmsTemplate.convertAndSend(queueName + "DeleteAllFilesByUsername-request", request);
    }


}
