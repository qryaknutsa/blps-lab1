package com.example.blpslab1.delegate;

import com.example.blpslab1.config.JackRabbitRepositoryBuilder;
import com.example.blpslab1.model.FileResponse;
import com.example.blpslab1.model.RabbitNode;
import com.example.blpslab1.service.FileService;
import com.example.blpslab1.service.OwnershipService;
import com.example.blpslab1.utils.JackRabbitUtils;
import jakarta.ws.rs.NotFoundException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component
public class GetFileDelegate implements JavaDelegate {
    Repository repo;

    @Autowired
    FileService fileService;

    @Autowired
    OwnershipService ownershipService;


    public GetFileDelegate(FileService fileService, OwnershipService ownershipService) throws RepositoryException {
        this.fileService = fileService;
        this.ownershipService = ownershipService;
        repo = JackRabbitRepositoryBuilder.getRepo("localhost", 27017);
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {
        String login = (String) delegateExecution.getVariable("login");
        boolean isAdmin = (boolean) delegateExecution.getVariable("is_admin");
        if (isAdmin) login = (String) delegateExecution.getVariable("admin_get_file_username");


        Session session = JackRabbitUtils.getSession(repo);
        FileResponse response;

        RabbitNode input = new RabbitNode(
                (String) delegateExecution.getVariable("parent_id_get"),
                (String) delegateExecution.getVariable("filename_get"),
                (String) delegateExecution.getVariable("mime_type_get"),
                (String) delegateExecution.getVariable("id_file_get"));

        System.out.println("getNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());


        try {
            ownershipService.getRecord(login, input.getParentId());
        } catch (NotFoundException ignored) {
            JackRabbitUtils.cleanUp(session);
            return;
        }

        response = fileService.getNode(session, "1.0", input);

        String ee = new String(response.getBytes());
        delegateExecution.setVariable("file", ee);

    }
}
