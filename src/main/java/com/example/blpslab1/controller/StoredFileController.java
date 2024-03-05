package com.example.blpslab1.controller;

import com.example.blpslab1.model.FilePath;
import com.example.blpslab1.model.StoredFile;
import com.example.blpslab1.service.ResponseStatus;
import com.example.blpslab1.service.StoredFileService;
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

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadFile(@RequestBody FilePath filePath) throws IOException {
        ResponseStatus response = storedFileService.upload(filePath.getFilePath());
        if (response == GOOD)
            return ResponseEntity.ok().build();
        else if (response == ALREADY_EXISTS)
            return ResponseEntity.notFound().build();
        else return ResponseEntity.badRequest().build();
    }

    @GetMapping("{title}")
    public ResponseEntity<StoredFile> getFile(@PathVariable String title) {
        try{
            StoredFile storedFile = storedFileService.getStoredFile(title);
            return ResponseEntity.ok().body(storedFile);
        } catch(NullPointerException e){
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
