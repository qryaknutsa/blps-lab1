package com.example.blpslab1.delegate;

import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.UserRepo;
import com.example.blpslab1.subModel.Role;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSearchDelegate implements JavaDelegate {

    private final UserRepo repo;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        try {
            String login = (String) delegateExecution.getVariable("login");
            String password = (String) delegateExecution.getVariable("password");

            User user = repo.findUserByUsername(login).orElseThrow(UserNotFoundException::new);
//            if(!user.getPassword().equals(passwordEncoder.encode(password)))
//                throw new UserNotFoundException();

            boolean isAdmin = user.getRoleName() == Role.ADMIN;
            delegateExecution.setVariable("is_admin", isAdmin);
            delegateExecution.setVariable("user_exists", true);
        } catch (UserNotFoundException e){
            delegateExecution.setVariable("user_exists", false);

        }
    }
}
