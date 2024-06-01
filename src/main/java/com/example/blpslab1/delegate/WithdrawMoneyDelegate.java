package com.example.blpslab1.delegate;

import com.example.blpslab1.exceptions.OutOfBalanceException;
import com.example.blpslab1.exceptions.TransactionFailedException;
import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.service.UserService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jcr.RepositoryException;

@Component
public class WithdrawMoneyDelegate implements JavaDelegate {
    @Autowired
    UserService userService;

    public WithdrawMoneyDelegate(UserService userService) throws RepositoryException {
        this.userService = userService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String username = (String) delegateExecution.getVariable("login");
        boolean isAdmin = (boolean) delegateExecution.getVariable("is_admin");
        Integer change = (Integer) delegateExecution.getVariable("balance_change");
        if (isAdmin) username = (String) delegateExecution.getVariable("username");
        System.out.println("withdrawMoney called!");

        try {
            userService.withdrawMoney(username, Double.valueOf(change));
        } catch (UserNotFoundException | OutOfBalanceException | TransactionFailedException e) {
            System.out.println("Ошибка: " + e);
        }
    }
}
