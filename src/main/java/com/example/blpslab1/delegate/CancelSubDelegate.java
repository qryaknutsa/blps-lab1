package com.example.blpslab1.delegate;

import com.example.blpslab1.config.JackRabbitRepositoryBuilder;
import com.example.blpslab1.exceptions.OutOfBalanceException;
import com.example.blpslab1.exceptions.SubAlreadyExistsException;
import com.example.blpslab1.exceptions.TransactionFailedException;
import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.model.User;
import com.example.blpslab1.service.FileService;
import com.example.blpslab1.service.OwnershipService;
import com.example.blpslab1.service.UserService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import static com.example.blpslab1.subModel.Role.ADMIN;

@Component
public class CancelSubDelegate implements JavaDelegate {
    @Autowired
    UserService userService;

    public CancelSubDelegate(UserService userService) throws RepositoryException {
        this.userService = userService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String username = (String) delegateExecution.getVariable("login");

        boolean isAdmin = (boolean) delegateExecution.getVariable("is_admin");
        if (isAdmin) username = (String) delegateExecution.getVariable("username_cancel_sub");
        System.out.println("cancelSub called!");

        try {
            userService.cancelSub(username);
        } catch (UserNotFoundException | TransactionFailedException |
                 SubAlreadyExistsException e) {
            delegateExecution.setVariable("result", "cancel sub exception" +  e.getMessage());
            System.out.println("cancel sub exception"+ e);
        }

    }

}
