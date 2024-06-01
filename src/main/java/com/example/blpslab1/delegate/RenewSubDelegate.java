package com.example.blpslab1.delegate;

import com.example.blpslab1.exceptions.SubAlreadyExistsException;
import com.example.blpslab1.exceptions.TransactionFailedException;
import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.service.UserService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jcr.RepositoryException;
@Component
public class RenewSubDelegate  implements JavaDelegate {
    @Autowired
    UserService userService;

    public RenewSubDelegate(UserService userService) throws RepositoryException {
        this.userService = userService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        System.out.println("renew_sub called!");
        userService.renew_sub();
    }
}
