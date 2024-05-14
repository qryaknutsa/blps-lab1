package com.example.blpslab1.controller;


import com.example.blpslab1.config.JackRabbitRepositoryBuilder;
import com.example.blpslab1.exceptions.UserNotFoundException;
import com.example.blpslab1.model.FileResponse;
import com.example.blpslab1.model.Ownership;
import com.example.blpslab1.model.RabbitNode;
import com.example.blpslab1.model.User;
import com.example.blpslab1.service.FileService;
import com.example.blpslab1.service.OwnershipService;
import com.example.blpslab1.utils.JackRabbitUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;
import java.util.Collection;
import java.util.List;


import javax.jcr.RepositoryException;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.Node;

import static com.example.blpslab1.subModel.FileType.*;
import static com.example.blpslab1.subModel.Role.ADMIN;
import static com.example.blpslab1.subModel.Role.USER;

@RestController
@RequestMapping("api/files")
public class FileController {
    Repository repo;

    @Autowired
    FileService fileService;

    @Autowired
    OwnershipService ownershipService;

    public FileController(FileService fileService, OwnershipService ownershipService) throws RepositoryException {
        this.fileService = fileService;
        this.ownershipService = ownershipService;
        repo = JackRabbitRepositoryBuilder.getRepo("localhost", 27017);
    }




    @Transactional
    @RequestMapping(method = RequestMethod.POST, value = {"/createFolder", "/createFolder/{username}"})
    public ResponseEntity<?> createFolderNode(@PathVariable @Nullable String username, @RequestBody RabbitNode input) throws RepositoryException {
        System.out.println("createFolderNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());
        User loggedUser = getLoggedUser();

        try {
            if (loggedUser != null) {
                if ((loggedUser.getRoleName() == USER && ownershipService.isExist(loggedUser.getUsername(), input.getParentId())) ||
                        (loggedUser.getRoleName() == ADMIN && ownershipService.isExist(username, input.getParentId()))) {

                    String name = loggedUser.getRoleName() == ADMIN ? username : loggedUser.getUsername();
                    
                    Session session = JackRabbitUtils.getSession(repo);

                    Node node = fileService.createFolderNode(session, input);
                    String identifier = node.getIdentifier();

                    ownershipService.addRecord(name, identifier, FOLDER, input.getFileName());

                    JackRabbitUtils.cleanUp(session);
                    return ResponseEntity.ok().body(identifier);
                } else return ResponseEntity.status(403).body("Нет доступа к этой директории");
            } else return ResponseEntity.status(403).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @Transactional
    @RequestMapping(method = RequestMethod.POST, value = {"/createFile", "/createFile/{username}"})
    public ResponseEntity<?> createNode(@PathVariable @Nullable String username, @RequestParam(value = "parent") String parent, @RequestParam(value = "file") MultipartFile file) throws RepositoryException {
        Session session = JackRabbitUtils.getSession(repo);
        RabbitNode input = new RabbitNode(parent, file.getOriginalFilename(), URLConnection.guessContentTypeFromName(file.getName()), "");

        System.out.println("createNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());
        User loggedUser = getLoggedUser();

        try {
            if (loggedUser != null) {
                if ((loggedUser.getRoleName() == USER && ownershipService.isExist(loggedUser.getUsername(), parent)) ||
                        (loggedUser.getRoleName() == ADMIN && ownershipService.isExist(username, parent))) {
                    String name = loggedUser.getRoleName() == ADMIN ? username : loggedUser.getUsername();


                    Node node = fileService.createNode(session, input, file);
                    String identifier = node.getIdentifier();

                    ownershipService.addRecord(name, identifier, FILE, input.getFileName());

                    session.getNodeByIdentifier(input.getParentId());
                    JackRabbitUtils.cleanUp(session);
                    return ResponseEntity.ok().body(identifier);
                } else return ResponseEntity.status(403).body("Нет доступа к этой директории");
            } else return ResponseEntity.status(403).body("Нет доступа");
        } catch (Exception e) {
            JackRabbitUtils.cleanUp(session);
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @Transactional
    @RequestMapping(method = RequestMethod.DELETE, value = {"/deleteFile", "/deleteFile/{username}"})
    public ResponseEntity<?> deleteNode(@PathVariable @Nullable String username, @RequestBody RabbitNode input) {
        Session session = JackRabbitUtils.getSession(repo);

        System.out.println("deleteNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());
        User loggedUser = getLoggedUser();

        try {
            if (loggedUser != null) {
                if ((loggedUser.getRoleName() == USER && ownershipService.isExist(loggedUser.getUsername(), input.getFileId())) ||
                        (loggedUser.getRoleName() == ADMIN && ownershipService.isExist(username, input.getFileId()))) {
                    String name = loggedUser.getRoleName() == ADMIN ? username : loggedUser.getUsername();


                    boolean result = fileService.deleteNode(session, input);

                    ownershipService.deleteRecord(name, input.getFileId());

                    JackRabbitUtils.cleanUp(session);

                    return ResponseEntity.ok().body(result);
                } else return ResponseEntity.status(403).body("В папке нет такого файла.");
            } else return ResponseEntity.status(403).body("Нет доступа.");
        } catch (Exception e) {
            JackRabbitUtils.cleanUp(session);
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @PostMapping(value = "/getVersions")
    public List<String> getVersionHistory(@RequestBody RabbitNode input) {
        Session session = JackRabbitUtils.getSession(repo);

        System.out.println("getVersionHistory called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());

        JackRabbitUtils.cleanUp(session);
        return fileService.getVersionHistory(session, input);
    }

    @Transactional
    @RequestMapping(method = RequestMethod.POST, value = {"/getFile/{versionId}","/getFile/{versionId}/{username}"})
    public ResponseEntity<?> getNode(@PathVariable String versionId, @PathVariable @Nullable String username, @RequestBody RabbitNode input) {
        Session session = JackRabbitUtils.getSession(repo);
        FileResponse response;

        System.out.println("getNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());
        User loggedUser = getLoggedUser();
        try {
            if (loggedUser != null) {
                if ((loggedUser.getRoleName() == USER && ownershipService.isExist(loggedUser.getUsername(), input.getFileId())) ||
                        (loggedUser.getRoleName() == ADMIN && ownershipService.isExist(username, input.getFileId()))) {
                    response = fileService.getNode(session, versionId, input);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(response.getContentType()));
                    JackRabbitUtils.cleanUp(session);
                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(response.getBytes());
                } else return ResponseEntity.status(403).body("Нет доступа к этой директории");
            } else return ResponseEntity.status(403).body("Нет доступа");
        } catch (Exception e) {
            JackRabbitUtils.cleanUp(session);
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = {"/getAllFiles","/getAllFiles/{username}"})
    public ResponseEntity<?> getAllNodes(@PathVariable @Nullable String username) {
        System.out.println("geAllNodes called!");
        User loggedUser = getLoggedUser();
        try {
            if(loggedUser != null && loggedUser.getRoleName() == ADMIN && username != null){
                Collection<Ownership> records = ownershipService.getAllRecords(username);
                return ResponseEntity.ok(records);
            } else if (loggedUser != null && username == null) {
                Collection<Ownership> records = ownershipService.getAllRecords(loggedUser.getUsername());
                return ResponseEntity.ok(records);
            } else return ResponseEntity.status(403).body("Нет доступа");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    private User getLoggedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
