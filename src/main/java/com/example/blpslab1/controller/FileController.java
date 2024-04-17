package com.example.blpslab1.controller;

import com.example.blpslab1.exceptions.*;
import com.example.blpslab1.model.User;
import com.example.blpslab1.model.subModel.FilePath;
import com.example.blpslab1.service.FileService;
import com.example.blpslab1.serviceConnection.Message;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.Nullable;

import java.io.IOException;

import static com.example.blpslab1.model.subModel.Role.ADMIN;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("api/files")
@AllArgsConstructor
public class FileController {
    private final FileService fileService;

    @RequestMapping(method = GET, value = {"/all-by-user", "/all-by-user/{username}"})
    public ResponseEntity<?> getAllFiles(@PathVariable @Nullable String username) {
        try {
            User loggedUser = getLoggedUser();
            if (username == null)
                return ResponseEntity.ok().body(fileService.getAllFilesNameByUsername(loggedUser.getUsername()));
            else {
                if (loggedUser.getRoleName() == ADMIN) {
                    return ResponseEntity.ok().body(fileService.getAllFilesNameByUsername(username));
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsersFiles() {
        return ResponseEntity.ok().body(fileService.getAllFilesName());
    }

    @RequestMapping(method = POST, value = {"/upload", "/upload/{username}"})
    public ResponseEntity<?> uploadFileUser(@PathVariable @Nullable String username, @RequestBody @NonNull FilePath filePath) throws IOException {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                fileService.upload(loggedUser.getUsername(), filePath.getFilePath());
                return ResponseEntity.ok().body("Файл загружается");
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    fileService.upload(username, filePath.getFilePath());
                    return ResponseEntity.ok().body("Файл загружается");
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = GET, value = {"/file/{title}", "/file/{username}/{title}"})
    public ResponseEntity<?> getUserFile(@PathVariable @Nullable String username, @PathVariable @NonNull String title) {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                Message file = fileService.getStoredFile(loggedUser.getUsername(), title);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/plain"));
                headers.setContentDispositionFormData("attachment", file.getTitle());

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(file.getData());
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    Message file = fileService.getStoredFile(username, title);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType("text/plain"));
                    headers.setContentDispositionFormData("attachment", file.getTitle());

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(file.getData());
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @RequestMapping(method = DELETE, value = {"/{title}", "/{username}/{title}"})
    public ResponseEntity<?> deleteFileUser(@PathVariable @Nullable String username, @PathVariable @NonNull String title) {
        try {
            User loggedUser = getLoggedUser();
            if (username == null) {
                fileService.deleteFile(loggedUser.getUsername(), title);
                return ResponseEntity.ok().body("Файл удаляется");
            } else {
                if (loggedUser.getRoleName() == ADMIN) {
                    fileService.deleteFile(username, title);
                    return ResponseEntity.ok().body("Файл удаляется");
                } else return ResponseEntity.status(403).body("Нет доступа");
            }
        } catch (UserNotFoundException  e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    private User getLoggedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
