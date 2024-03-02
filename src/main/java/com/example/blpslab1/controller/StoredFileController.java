package com.example.blpslab1.controller;

import com.example.blpslab1.model.FilePath;
import com.example.blpslab1.model.StoredFile;
import com.example.blpslab1.model.User;
import com.example.blpslab1.service.ResponseStatus;
import com.example.blpslab1.service.StoredFileService;
import com.example.blpslab1.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.example.blpslab1.service.ResponseStatus.*;

@RestController
@RequestMapping("api/files")
@AllArgsConstructor
public class StoredFileController {
    private final StoredFileService storedFileService;

    @GetMapping("/all")
    public ResponseEntity<List<String>> getAllUsers() {
        return ResponseEntity.ok().body(storedFileService.getAllFilesName());
    }

    @PostMapping("upload/")
    public ResponseEntity<Void> signUp(@RequestBody FilePath filePath) throws IOException {
        System.out.println(filePath.getFilePath());
        ResponseStatus response = storedFileService.upload(filePath.getFilePath());
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else return ResponseEntity.internalServerError().build();
    }

    @GetMapping("{title}")
    public ResponseEntity<StoredFile> getUser(@PathVariable String title) {
        try{
            StoredFile storedFile = storedFileService.getStoredFile(title);
            return ResponseEntity.ok().body(storedFile);
        } catch(NullPointerException e){
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
