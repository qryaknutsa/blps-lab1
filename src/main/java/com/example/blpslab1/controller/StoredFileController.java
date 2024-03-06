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
    public ResponseEntity<String> uploadFile(@RequestBody FilePath filePath) throws IOException {
        ResponseStatus response = storedFileService.upload(filePath.getFilePath());
        if (response == GOOD)
            return ResponseEntity.ok("Файл загружен");
        else if (response == ALREADY_EXISTS)
            return ResponseEntity.ok("Файл не загружен. Файл с таким именем уже загружен");
        else if (response == NOT_FOUND)
            return ResponseEntity.ok("Файл не загружен. Файл не найден");
        else return ResponseEntity.ok("Файл не загружен. Войдите в систему");
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

    @DeleteMapping("{title}")
    public ResponseEntity<String> deleteFile(@PathVariable String title) {
        ResponseStatus status = storedFileService.deleteFile(title);
        if(status == GOOD) return ResponseEntity.ok("Файл удален");
        else if(status == NOT_FOUND) return ResponseEntity.ok("Файл не был найден");
        else return ResponseEntity.ok("Файл не удален. Войдите в систему");
    }

}
