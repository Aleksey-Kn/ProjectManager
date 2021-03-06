package ru.manager.ProgectManager.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class PhotoService {
    public ResponseEntity<?> sendFile(HttpServletResponse response, byte[] photo) {
        try {
            response.setContentType("image/jpeg");
            response.setHeader("Content-Disposition", "inline; filename=\"" + UUID.randomUUID() + ".jpg\"");
            response.setContentLength(photo.length);
            FileCopyUtils.copy(new ByteArrayInputStream(photo), response.getOutputStream());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
