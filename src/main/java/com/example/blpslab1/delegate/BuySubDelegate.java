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
public class BuySubDelegate implements JavaDelegate {
    @Autowired
    UserService userService;

    public BuySubDelegate(UserService userService) throws RepositoryException {
        this.userService = userService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String username = (String) delegateExecution.getVariable("login");
        boolean isAdmin = (boolean) delegateExecution.getVariable("is_admin");
        if (isAdmin) username = (String) delegateExecution.getVariable("username_buy_sub");
        System.out.println("buySub called!");
        try {
            userService.buySub(username);
            delegateExecution.setVariable("result", "Успех");
        } catch (UserNotFoundException | OutOfBalanceException | TransactionFailedException |
                 SubAlreadyExistsException e) {

            delegateExecution.setVariable("result", "buy sub exception" +  e.getMessage());
            System.out.println("buy sub exception" + e);

        }
    }
}
