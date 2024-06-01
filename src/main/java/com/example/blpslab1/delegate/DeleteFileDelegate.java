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

import javax.jcr.*;
import javax.jcr.version.VersionManager;
import java.io.ByteArrayInputStream;
import java.util.Date;

import static com.example.blpslab1.subModel.FileType.FILE;

@Component
public class DeleteFileDelegate implements JavaDelegate {

    Repository repo;

    @Autowired
    FileService fileService;

    @Autowired
    OwnershipService ownershipService;

    @Autowired
    UserRepo userRepo;

    public DeleteFileDelegate(FileService fileService, OwnershipService ownershipService, UserRepo userRepo) throws RepositoryException {
        this.fileService = fileService;
        this.ownershipService = ownershipService;
        repo = JackRabbitRepositoryBuilder.getRepo("localhost", 27017);
        this.userRepo = userRepo;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Session session = JackRabbitUtils.getSession(repo);

        String login = (String) delegateExecution.getVariable("login");
        RabbitNode input = new RabbitNode(
                (String) delegateExecution.getVariable("parent_id_delete"),
                (String) delegateExecution.getVariable("filename_delete"),
                (String) delegateExecution.getVariable("mime_type_delete"),
                (String) delegateExecution.getVariable("id_file_delete"));
        System.out.println("deleteNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());

        //ADMIN
        boolean isAdmin = (boolean) delegateExecution.getVariable("is_admin");
        if (isAdmin) login = (String) delegateExecution.getVariable("admin_delete_file_username");

        String identifier = input.getFileId();
        Ownership ownership = new Ownership(login, input.getFileId(), FILE, input.getFileName());
        FileResponse before = fileService.getNode(session, "1.0", input);
        try {
            try {
                ownershipService.getRecord(login, input.getParentId());
            } catch (NotFoundException ignored) {
                JackRabbitUtils.cleanUp(session);
                return;
            }

            fileService.deleteNode(session, input);
            ownershipService.deleteRecord(login, input.getFileId());
            JackRabbitUtils.cleanUp(session);
            delegateExecution.setVariable("result", "Успех");
        } catch (Exception e) {
            System.out.println(identifier);
            input.setFileId(identifier);

            //before
            if (ownershipService.isExist(ownership.getUserLogin(), ownership.getFileId()))
                ownershipService.deleteRecord(ownership.getUserLogin(), ownership.getFileId());
            String id = "";
            Node parentNode;
            try {
                parentNode = session.getNodeByIdentifier(input.getParentId());
            } catch (RepositoryException ex) {
                throw new RuntimeException(ex);
            }

            try {
                Node node = parentNode.addNode(input.getFileName(), "nt:file");
                node.addMixin("mix:versionable");
                node.addMixin("mix:referenceable");
                id = node.getIdentifier();
                Node content = node.addNode("jcr:content", "nt:resource");

                Binary binary = session.getValueFactory().createBinary(new ByteArrayInputStream(before.getBytes()));

                content.setProperty("jcr:data", binary);
                content.setProperty("jcr:mimeType", input.getMimeType());

                Date now = new Date();
                now.toInstant().toString();
                content.setProperty("jcr:lastModified", now.toInstant().toString());

                session.save();

                VersionManager vm = session.getWorkspace().getVersionManager();
                vm.checkin(node.getPath());

            } catch (Exception ee) {
                ee.printStackTrace();
            }
            JackRabbitUtils.cleanUp(session);

            ownershipService.addRecord(login, id, FILE, input.getFileName());
            delegateExecution.setVariable("result", "delete file exception" +  e.getMessage());
        }
    }
}
