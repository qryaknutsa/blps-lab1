package com.example.blpslab1.delegate;


import com.example.blpslab1.config.JackRabbitRepositoryBuilder;
import com.example.blpslab1.repo.UserRepo;
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
public class CopyDirDelegate implements JavaDelegate {
    Repository repo;

    @Autowired
    FileService fileService;

    @Autowired
    OwnershipService ownershipService;

    @Autowired
    UserRepo userRepo;

    public CopyDirDelegate(FileService fileService, OwnershipService ownershipService, UserRepo userRepo) throws RepositoryException {
        this.fileService = fileService;
        this.ownershipService = ownershipService;
        repo = JackRabbitRepositoryBuilder.getRepo("localhost", 27017);
        this.userRepo = userRepo;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Session session = JackRabbitUtils.getSession(repo);

        //ADMIN
        boolean isAdmin = (boolean) delegateExecution.getVariable("is_admin");
        String login = (String) delegateExecution.getVariable("login");
        boolean flag = false;

        String srcDir = (String) delegateExecution.getVariable("src_copy_dir");
        String dstDir = (String) delegateExecution.getVariable("dst_copy_dir");
        String usernameCopyDir = (String) delegateExecution.getVariable("username_copy_dir");

        System.out.println("copyFolder called!");

        if (isAdmin) flag = true;
        else {
            try {
                ownershipService.getRecord(login, srcDir);
                flag = true;
            } catch (NotFoundException ignored) {
            }
        }
        System.out.println(flag);
        if (flag) fileService.copyDir(session, usernameCopyDir, srcDir, dstDir);

        JackRabbitUtils.cleanUp(session);

    }
}
