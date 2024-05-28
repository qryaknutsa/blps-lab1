package com.example.blpslab1.controller;


import com.example.blpslab1.config.JackRabbitRepositoryBuilder;
import com.example.blpslab1.model.FileResponse;
import com.example.blpslab1.model.Ownership;
import com.example.blpslab1.model.RabbitNode;
import com.example.blpslab1.model.User;
import com.example.blpslab1.repo.UserRepo;
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.List;


import javax.jcr.*;
import javax.jcr.version.VersionManager;

import static com.example.blpslab1.subModel.FileType.*;
import static com.example.blpslab1.subModel.Role.ADMIN;
import static com.example.blpslab1.subModel.Role.USER;

@RestController
@RequestMapping("api/files")
public class FileController {

    long defaultFileSize = 1024 * 1500;

    Repository repo;

    @Autowired
    FileService fileService;

    @Autowired
    OwnershipService ownershipService;
    private final UserRepo userRepo;

    public FileController(FileService fileService, OwnershipService ownershipService,
                          UserRepo userRepo) throws RepositoryException {
        this.fileService = fileService;
        this.ownershipService = ownershipService;
        repo = JackRabbitRepositoryBuilder.getRepo("localhost", 27017);
        this.userRepo = userRepo;
    }


    @Transactional
    @RequestMapping(method = RequestMethod.POST, value = {"/createFolder", "/createFolder/{username}"})
    public ResponseEntity<?> createFolderNode(@PathVariable @Nullable String username, @RequestBody RabbitNode input) throws RepositoryException {
        System.out.println("createFolderNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());
        User loggedUser = getLoggedUser();

        Session session = JackRabbitUtils.getSession(repo);


        Ownership ownership = new Ownership();
        Node node;
        try {
            if (loggedUser != null) {
                if ((loggedUser.getRoleName() == USER && ownershipService.isExist(loggedUser.getUsername(), input.getParentId())) ||
                        (loggedUser.getRoleName() == ADMIN && ownershipService.isExist(username, input.getParentId()))) {
                    String name = loggedUser.getRoleName() == ADMIN ? username : loggedUser.getUsername();

                    node = fileService.createFolderNode(session, input);

                    String identifier = node.getIdentifier();
                    ownership = ownershipService.addRecord(name, identifier, FOLDER, input.getFileName());

                    JackRabbitUtils.cleanUp(session);
                    return ResponseEntity.ok().body(identifier);
                } else return ResponseEntity.status(403).body("Нет доступа к этой директории");
            } else return ResponseEntity.status(403).body("Нет доступа");
        } catch (Exception e) {
            if (ownershipService.isExist(ownership.getUserLogin(), ownership.getFileId()))
                ownershipService.deleteRecord(ownership.getUserLogin(), ownership.getFileId());


            FileResponse response = fileService.getNode(session, "1.0", input);
            if (response != null) fileService.deleteNode(session, input);

            JackRabbitUtils.cleanUp(session);
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

        Node node;
        Ownership ownership = new Ownership();
        String identifier = "";
        try {
            if (loggedUser != null) {
                if ((loggedUser.getRoleName() == USER && ownershipService.isExist(loggedUser.getUsername(), parent)) ||
                        (loggedUser.getRoleName() == ADMIN && ownershipService.isExist(username, parent))) {
                    String name = loggedUser.getRoleName() == ADMIN ? username : loggedUser.getUsername();

                    boolean sub = userRepo.findAll().stream().filter(u -> u.getUsername().equals(name)).findFirst().get().getSubscription();
                    if (!sub && file.getSize() > defaultFileSize)
                        return ResponseEntity.status(403).body("Файл весит больше 5Кб. Приобретите подписку для хранения больших файлов.");

                    node = fileService.createNode(session, input, file);
                    identifier = node.getIdentifier();
//                    method();
                    ownership = ownershipService.addRecord(name, identifier, FILE, input.getFileName());

                    session.getNodeByIdentifier(input.getParentId());
                    JackRabbitUtils.cleanUp(session);
                    return ResponseEntity.ok().body(identifier);
                } else return ResponseEntity.status(403).body("Нет доступа к этой директории");
            } else return ResponseEntity.status(403).body("Нет доступа");
        } catch (Exception e) {
            if (ownershipService.isExist(ownership.getUserLogin(), ownership.getFileId()))
                ownershipService.deleteRecord(ownership.getUserLogin(), ownership.getFileId());

            System.out.println(identifier);
            input.setFileId(identifier);
            FileResponse response = fileService.getNode(session, "1.0", input);
            if (response != null) fileService.deleteNode(session, input);

            JackRabbitUtils.cleanUp(session);
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());

        }
    }

    void method() {
        throw new RuntimeException();
    }

    @Transactional
    @RequestMapping(method = RequestMethod.DELETE, value = {"/deleteFile", "/deleteFile/{username}"})
    public ResponseEntity<?> deleteNode(@PathVariable @Nullable String username, @RequestBody RabbitNode input) {
        Session session = JackRabbitUtils.getSession(repo);

        System.out.println("deleteNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());
        User loggedUser = getLoggedUser();
        String name = loggedUser.getRoleName() == ADMIN ? username : loggedUser.getUsername();

        String identifier = input.getFileId();
        Ownership ownership = new Ownership(name, input.getFileId(), FILE, input.getFileName());
        FileResponse before = fileService.getNode(session, "1.0", input);
        try {
            if (loggedUser != null) {
                if ((loggedUser.getRoleName() == USER && ownershipService.isExist(loggedUser.getUsername(), input.getFileId())) ||
                        (loggedUser.getRoleName() == ADMIN && ownershipService.isExist(username, input.getFileId()))) {


                    boolean result = fileService.deleteNode(session, input);

                    method();
                    ownershipService.deleteRecord(name, input.getFileId());

                    JackRabbitUtils.cleanUp(session);

                    return ResponseEntity.ok().body(result);
                } else return ResponseEntity.status(403).body("В папке нет такого файла.");
            } else return ResponseEntity.status(403).body("Нет доступа.");
        } catch (Exception e) {
            System.out.println(identifier);
            input.setFileId(identifier);

            //before
            if (ownershipService.isExist(ownership.getUserLogin(), ownership.getFileId()))
                ownershipService.deleteRecord(ownership.getUserLogin(), ownership.getFileId());
            String id = "";
            Node parentNode;
            try {
                parentNode = session.getNodeByIdentifier(input.getParentId());
            } catch (RepositoryException ex) {
                throw new RuntimeException(ex);
            }

            try {
                Node node = parentNode.addNode(input.getFileName(), "nt:file");
                node.addMixin("mix:versionable");
                node.addMixin("mix:referenceable");
                id = node.getIdentifier();
                Node content = node.addNode("jcr:content", "nt:resource");

                Binary binary = session.getValueFactory().createBinary(new ByteArrayInputStream(before.getBytes()));

                content.setProperty("jcr:data", binary);
                content.setProperty("jcr:mimeType", input.getMimeType());

                Date now = new Date();
                now.toInstant().toString();
                content.setProperty("jcr:lastModified", now.toInstant().toString());

                session.save();

                VersionManager vm = session.getWorkspace().getVersionManager();
                vm.checkin(node.getPath());

            } catch (Exception ee) {
                ee.printStackTrace();
            }
            JackRabbitUtils.cleanUp(session);

            ownershipService.addRecord(name, id, FILE, input.getFileName());
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
    @RequestMapping(method = RequestMethod.POST, value = {"/getFile/{versionId}", "/getFile/{versionId}/{username}"})
    public ResponseEntity<?> getNode(@PathVariable String versionId, @PathVariable @Nullable String username, @RequestBody RabbitNode input) {
        Session session = JackRabbitUtils.getSession(repo);
        FileResponse response;

        System.out.println("getNode called!");
        System.out.println("parentId: " + input.getParentId() + "\nfilePath: " + input.getFileName() + "\nmimeType: " + input.getMimeType() + "\nfileId: " + input.getFileId());
        User loggedUser = getLoggedUser();
        try {
            if (loggedUser != null) {
//                if ((loggedUser.getRoleName() == USER && ownershipService.isExist(loggedUser.getUsername(), input.getFileId())) ||
//                        (loggedUser.getRoleName() == ADMIN && ownershipService.isExist(username, input.getFileId()))) {
                response = fileService.getNode(session, versionId, input);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(response.getContentType()));
                JackRabbitUtils.cleanUp(session);
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(response.getBytes());
//                } else return ResponseEntity.status(403).body("Нет доступа к этой директории");
            } else return ResponseEntity.status(403).body("Нет доступа");
        } catch (Exception e) {
            JackRabbitUtils.cleanUp(session);
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = {"/getAllFiles", "/getAllFiles/{username}"})
    public ResponseEntity<?> getAllNodes(@PathVariable @Nullable String username) {
        System.out.println("geAllNodes called!");
        User loggedUser = getLoggedUser();
        try {
            if (loggedUser != null && loggedUser.getRoleName() == ADMIN && username != null) {
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


    @RequestMapping(method = RequestMethod.POST, value = {"/copyFile"})
    public ResponseEntity<?> copyDir(@RequestParam(value = "username") String username, @RequestParam(value = "sourceDir") String sourceDir, @RequestParam(value = "targetParentId") String targetParentId) throws RepositoryException {
        Session session = JackRabbitUtils.getSession(repo);

        fileService.copyDir(session, username, sourceDir, targetParentId);

        return ResponseEntity.ok().body("Ждите");

    }

    private User getLoggedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
