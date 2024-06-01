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

import static com.example.blpslab1.subModel.FileType.FOLDER;


@Component
public class CreateDirDelegate implements JavaDelegate {

    Repository repo;

    @Autowired
    FileService fileService;

    @Autowired
    OwnershipService ownershipService;

    @Autowired
    UserRepo userRepo;

    public CreateDirDelegate(FileService fileService, OwnershipService ownershipService, UserRepo userRepo) throws RepositoryException {
        this.fileService = fileService;
        this.ownershipService = ownershipService;
        repo = JackRabbitRepositoryBuilder.getRepo("localhost", 27017);
        this.userRepo = userRepo;
    }


    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String login = (String) delegateExecution.getVariable("login");
        RabbitNode input = new RabbitNode(
                (String) delegateExecution.getVariable("parent_id_create_folder"),
                (String) delegateExecution.getVariable("foldername_create"),
                "",
                "");
        System.out.println("createFolderNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());

        Session session = JackRabbitUtils.getSession(repo);

        //ADMIN
        boolean isAdmin = (boolean) delegateExecution.getVariable("is_admin");
        if (isAdmin) login = (String) delegateExecution.getVariable("admin_create_dir_username");


        Ownership ownership = new Ownership();
        Node node;
        try {

            try {
                ownershipService.getRecord(login, input.getParentId());
            } catch (NotFoundException ignored) {
                JackRabbitUtils.cleanUp(session);
                return;
            }

            node = fileService.createFolderNode(session, input);

            String identifier = node.getIdentifier();
            ownership = ownershipService.addRecord(login, identifier, FOLDER, input.getFileName());

            System.out.println(identifier);
            JackRabbitUtils.cleanUp(session);
        } catch (Exception e) {
            if (ownershipService.isExist(ownership.getUserLogin(), ownership.getFileId()))
                ownershipService.deleteRecord(ownership.getUserLogin(), ownership.getFileId());


            FileResponse response = fileService.getNode(session, "1.0", input);
            if (response != null) fileService.deleteNode(session, input);

            JackRabbitUtils.cleanUp(session);
        }
    }
}
