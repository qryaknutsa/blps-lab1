package com.example.blpslab1.delegate;

import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignupDelegate implements JavaDelegate {
    private final UserRepo repo;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String login = (String) delegateExecution.getVariable("login");
        String password = (String) delegateExecution.getVariable("password");
        try {
            repo.findUserByUsername(login).orElseThrow(UserNotFoundException::new);
        } catch (UserNotFoundException e){
            User user = new User(login, password);
            repo.save(user);
        }
    }
}
