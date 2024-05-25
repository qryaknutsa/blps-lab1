package com.example.blpslab1.service;

import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.model.Ownership;
import com.example.blpslab1.repo.OwnershipRepo;
import com.example.blpslab1.subModel.FileType;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.Collection;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class OwnershipService {
    private final OwnershipRepo repo;


    public void addRecord(String userLogin, String fileId, FileType type, String filename){
        repo.save(new Ownership(userLogin, fileId, type, filename));
    }


    //TODO: add exception
    public Ownership getRecord(String userLogin, String fileId){
        return repo.findOwnershipByUserLoginAndFileId(userLogin, fileId).orElseThrow(NotFoundException::new);
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
