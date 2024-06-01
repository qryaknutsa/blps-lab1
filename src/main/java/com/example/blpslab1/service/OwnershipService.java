package com.example.blpslab1.service;

import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.model.Ownership;
import com.example.blpslab1.repo.OwnershipRepo;
import com.example.blpslab1.subModel.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service
@RequiredArgsConstructor
public class OwnershipService {
    private final OwnershipRepo repo;


    public Ownership addRecord(String userLogin, String fileId, FileType type, String filename){
        return repo.save(new Ownership(userLogin, fileId, type, filename));
    }


    //TODO: add exception
    public Ownership getRecord(String userLogin, String fileId){
        return repo.findOwnershipByUserLoginAndFileId(userLogin, fileId).orElseThrow();
    }

    public Collection<Ownership> getAllRecords(String userLogin){
        return repo.findAllByUserLogin(userLogin);
    }

    public boolean isExist(String userLogin, String fileId){
        try {
            repo.findOwnershipByUserLoginAndFileId(userLogin, fileId).orElseThrow(NullPointerException::new);
            return true;
        }catch (NullPointerException e){
            return false;
        }
    }
    public void deleteRecord(String userLogin, String fileId){
        Ownership ownership = repo.findOwnershipByUserLoginAndFileId(userLogin, fileId).orElseThrow(UserNotFoundException::new);
        repo.delete(ownership);
    }

    public String getRealRoot(){
        return repo.findAll().stream().filter(node -> node.getType()==FileType.REAL_ROOT).findFirst().get().getFileId();
    }
}
