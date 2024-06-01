package com.example.blpslab1.delegate;

import com.example.blpslab1.exceptions.OutOfBalanceException;
import com.example.blpslab1.exceptions.SubAlreadyExistsException;
import com.example.blpslab1.exceptions.TransactionFailedException;
import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.model.User;
import com.example.blpslab1.service.UserService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.jcr.RepositoryException;

import static com.example.blpslab1.subModel.Role.ADMIN;

@Component
public class PutMoneyDelegate implements JavaDelegate {
    @Autowired
    UserService userService;

    public PutMoneyDelegate(UserService userService) throws RepositoryException {
        this.userService = userService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String username = (String) delegateExecution.getVariable("login");
        boolean isAdmin = (boolean) delegateExecution.getVariable("is_admin");
        Integer change = (Integer) delegateExecution.getVariable("balance_change");
        if (isAdmin) username = (String) delegateExecution.getVariable("username");
        System.out.println("putMoney called!");

        try {
            userService.putMoney(username, Double.valueOf(change));
        } catch (UserNotFoundException | OutOfBalanceException | TransactionFailedException e) {
            System.out.println("Ошибка: " + e);
        }
    }
}
