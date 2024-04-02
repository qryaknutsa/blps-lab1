package com.example.blpslab1.controller;

import com.example.blpslab1.model.User;
import com.example.blpslab1.model.subModel.FilePath;
import com.example.blpslab1.model.StoredFile;
import com.example.blpslab1.dto.ResponseStatus;
import com.example.blpslab1.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.example.blpslab1.dto.ResponseStatus.*;

@RestController
@RequestMapping("api/files")
@AllArgsConstructor
public class FileController {
    private final FileService fileService;

    //USER PART
    @GetMapping("/user/all")
    public ResponseEntity<List<String>> getAllUsersFiles() {
        String username = getLoggedUsername();
        return ResponseEntity.ok().body(fileService.getAllFilesName(username));
    }

    @PostMapping("/user/upload")
    public ResponseEntity<String> uploadFileUser(@RequestBody FilePath filePath) throws IOException {
        User user = getLoggedUser();
        ResponseStatus response = fileService.upload(user.getUsername(), filePath.getFilePath());
        if (response == GOOD)
            return ResponseEntity.ok("Файл загружен");
        else if (response == ALREADY_EXISTS)
            return ResponseEntity.ok("Файл не загружен. Файл с таким именем уже загружен");
        else if (response == NOT_FOUND)
            return ResponseEntity.ok("Файл не загружен. Файл не найден");
        else return ResponseEntity.ok("Файл не загружен. Войдите в систему");
    }

    @GetMapping(value = "/user/{title}")
    public ResponseEntity<?> getFileUser(@PathVariable String title) {
        try {
            String username = getLoggedUsername();
            StoredFile file = fileService.getStoredFile(username, title);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/plain"));
            headers.setContentDispositionFormData("attachment", file.getTitle());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file.getData().getData());
        } catch (NullPointerException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @DeleteMapping("/user/{title}")
    public ResponseEntity<String> deleteFileUser(@PathVariable String title) {
        String username = getLoggedUsername();
        ResponseStatus status = fileService.deleteFile(username, title);
        if (status == GOOD) return ResponseEntity.ok("Файл удален");
        else if (status == NOT_FOUND) return ResponseEntity.ok("Файл не был найден");
        else return ResponseEntity.ok("Файл не удален. Войдите в систему");
    }


    //ADMIN PART
    @GetMapping("/admin/all")
    public ResponseEntity<List<String>> getAllFiles() {
        String username = getLoggedUsername();
        return ResponseEntity.ok().body(fileService.getAllFilesName());
    }

    @PostMapping("/admin/upload/{username}")
    public ResponseEntity<String> uploadFileUser(@PathVariable String username, @RequestBody FilePath filePath) throws IOException {
        ResponseStatus response = fileService.upload(username, filePath.getFilePath());
        if (response == GOOD)
            return ResponseEntity.ok("Файл загружен");
        else if (response == ALREADY_EXISTS)
            return ResponseEntity.ok("Файл не загружен. Файл с таким именем уже загружен");
        else if (response == NOT_FOUND)
            return ResponseEntity.ok("Файл не загружен. Файл не найден");
        else return ResponseEntity.ok("Файл не загружен. Войдите в систему");
    }

    @GetMapping("/admin/{username}/{title}")
    public ResponseEntity<?> getFileUser(@PathVariable String username, @PathVariable String title) {
        try{
            StoredFile file = fileService.getStoredFile(username, title);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/plain"));
            headers.setContentDispositionFormData("attachment", file.getTitle());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file.getData().getData());
        } catch(NullPointerException e){
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @DeleteMapping("/admin/{username}/{title}")
    public ResponseEntity<String> deleteFileUser(@PathVariable String username, @PathVariable String title) {
        ResponseStatus status = fileService.deleteFile(username, title);
        if (status == GOOD) return ResponseEntity.ok("Файл удален");
        else if (status == NOT_FOUND) return ResponseEntity.ok("Файл не был найден");
        else return ResponseEntity.ok("Файл не удален. Войдите в систему");
    }


    private User getLoggedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getLoggedUsername() {
        return getLoggedUser().getUsername();
    }

}
