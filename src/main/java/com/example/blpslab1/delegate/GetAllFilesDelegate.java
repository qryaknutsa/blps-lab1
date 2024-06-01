package com.example.blpslab1.delegate;

import com.example.blpslab1.model.Ownership;
import com.example.blpslab1.repo.OwnershipRepo;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class GetAllFilesDelegate implements JavaDelegate {
    private final OwnershipRepo repo;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        boolean isAdmin = (boolean) delegateExecution.getVariable("is_admin");

        if (isAdmin) {
            Collection<Ownership> collection = repo.findAll();
            delegateExecution.setVariable("allFiles", collection.toString());
            return;
        }
        String login = (String) delegateExecution.getVariable("login");
        Collection<Ownership> collection = repo.findAllByUserLogin(login);
        delegateExecution.setVariable("allFiles", collection.toString());


    }
}
