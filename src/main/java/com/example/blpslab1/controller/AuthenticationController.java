package com.example.blpslab1.controller;

import com.example.blpslab1.config.JackRabbitRepositoryBuilder;
import com.example.blpslab1.dto.AuthUserDTO;
import com.example.blpslab1.model.RabbitNode;
import com.example.blpslab1.service.AuthenticationService;
import com.example.blpslab1.dto.RegUserDTO;

import com.example.blpslab1.service.FileService;
import com.example.blpslab1.service.OwnershipService;
import com.example.blpslab1.service.UserService;
import com.example.blpslab1.subModel.FileType;
import com.example.blpslab1.utils.JackRabbitUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@RestController
@RequestMapping("api/auth")
public class AuthenticationController {

    Repository repo;
    @Autowired
    private final FileService fileService;
    @Autowired
    private final OwnershipService ownershipService;

    @Autowired
    private final AuthenticationService service;

    String root;
    public AuthenticationController(FileService fileService, OwnershipService ownershipService, AuthenticationService service) throws RepositoryException {
        this.fileService = fileService;
        this.ownershipService = ownershipService;
        this.service = service;
        repo = JackRabbitRepositoryBuilder.getRepo("localhost", 27017);
//        root = createRoot();
    }

    public String createRoot() throws RepositoryException {
        Session session = JackRabbitUtils.getSession(repo);

        System.out.println("createRoot called!");

        RabbitNode input = new RabbitNode("/", "oak", "", "");
        Node node = fileService.createFolderNode(session,input);

        String identifier = node.getIdentifier();
        JackRabbitUtils.cleanUp(session);
        ownershipService.addRecord("admin", identifier, FileType.REAL_ROOT, "REAL_ROOT");

        return identifier;
    }
    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@RequestBody AuthUserDTO request) throws RepositoryException {
        Session session = JackRabbitUtils.getSession(repo);

        service.register(request);

        String realRoot = ownershipService.getRealRoot();

        RabbitNode folder = new RabbitNode(realRoot, request.getUsername(), "", "");
        String fileId = fileService.createFolderNode(session, folder).getIdentifier();

        ownershipService.addRecord(request.getUsername(), fileId, FileType.ROOT, "ROOT");

        return ResponseEntity.ok().body("Пользователь успешно создан. Root -- " + fileId);
    }

}

