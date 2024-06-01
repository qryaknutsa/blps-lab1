package com.example.blpslab1.delegate;

import com.example.blpslab1.config.JackRabbitRepositoryBuilder;
import com.example.blpslab1.model.FileResponse;
import com.example.blpslab1.model.Ownership;
import com.example.blpslab1.model.RabbitNode;
import com.example.blpslab1.repo.UserRepo;
import com.example.blpslab1.service.FileService;
import com.example.blpslab1.service.OwnershipService;
import com.example.blpslab1.utils.JackRabbitUtils;
import jakarta.ws.rs.NotFoundException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;

import static com.example.blpslab1.subModel.FileType.FILE;

@Component
public class UploadFileDelegate implements JavaDelegate {
    Repository repo;

    @Autowired
    FileService fileService;

    @Autowired
    OwnershipService ownershipService;

    @Autowired
    UserRepo userRepo;

    public UploadFileDelegate(FileService fileService, OwnershipService ownershipService, UserRepo userRepo) throws RepositoryException {
        this.fileService = fileService;
        this.ownershipService = ownershipService;
        repo = JackRabbitRepositoryBuilder.getRepo("localhost", 27017);
        this.userRepo = userRepo;
    }


    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String parent = (String) delegateExecution.getVariable("parent_id");
        String login = (String) delegateExecution.getVariable("login");
        String filePath = (String) delegateExecution.getVariable("path");


        Session session = JackRabbitUtils.getSession(repo);

        File initialFile = new File(filePath);
        RabbitNode input = new RabbitNode(parent, initialFile.getName(), URLConnection.guessContentTypeFromName(initialFile.getName()), "");
        InputStream stream = new FileInputStream(initialFile);


        System.out.println("createNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());

        Node node;
        Ownership ownership = new Ownership();
        String identifier = "";
        try {
            try {
                ownershipService.getRecord(login, input.getParentId());
            } catch (NotFoundException ignored) {
                JackRabbitUtils.cleanUp(session);
                return;
            }

//            boolean sub = userRepo.findAll().stream().filter(u -> u.getUsername().equals(login)).findFirst().get().getSubscription();
//            if (!sub && file.getSize() > defaultFileSize)
//                return;


            node = fileService.createNode(session, input, stream, initialFile.getName());
            identifier = node.getIdentifier();
            System.out.println(identifier);
            ownership = ownershipService.addRecord(login, identifier, FILE, input.getFileName());

            session.getNodeByIdentifier(input.getParentId());
            JackRabbitUtils.cleanUp(session);

        } catch (Exception e) {
            if (ownershipService.isExist(ownership.getUserLogin(), ownership.getFileId()))
                ownershipService.deleteRecord(ownership.getUserLogin(), ownership.getFileId());

            System.out.println(identifier);
            input.setFileId(identifier);
            FileResponse response = fileService.getNode(session, "1.0", input);
            if (response != null) fileService.deleteNode(session, input);

            JackRabbitUtils.cleanUp(session);
        }
    }
}
