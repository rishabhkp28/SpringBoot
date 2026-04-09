package com.springbootdeveloper.Helpers;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.springbootdeveloper.Exceptions.EmptyFileException;
import com.springbootdeveloper.Exceptions.FileSizeException;
import com.springbootdeveloper.Exceptions.FileStorageException;
import com.springbootdeveloper.Exceptions.UnsupportedFileTypeException;

@Component
public class FileHandler 
{
    @Value("${file.upload-dir}")
    private String uploadDirectory;

    private static final long minSize = 10 * 1024;       // 10 KB
    private static final long maxSize = 2 * 1024 * 1024; // 2 MB	


    public String upload(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new EmptyFileException("File cannot be empty");
        }

        final long fileSize = multipartFile.getSize();

        if (fileSize > maxSize || fileSize < minSize) {
            throw new FileSizeException("File size must be between 10KB and 2MB");
        }

        String contentType = multipartFile.getContentType();
        if (contentType == null || !List.of("image/png", "image/jpeg", "image/jpg", "image/gif")
                .contains(contentType)) {
            throw new UnsupportedFileTypeException("Invalid image type");
        }

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/jpeg" -> ".jpeg";
            default          -> ".jpg";
        };

        String uniqueFilename = UUID.randomUUID().toString() + extension;

        Path uploadPath = Paths.get(uploadDirectory);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Files.copy(inputStream,
                           uploadPath.resolve(uniqueFilename),
                           StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new FileStorageException("Issue occurred while trying to save the file", e);
        }

        return uniqueFilename;
    }


    public Resource getFile(String filename) {
        try {
            // ── PATH TRAVERSAL PROTECTION ──────────────────────────
            // Step 1: strip any directory separators from filename
            //         e.g. "../../etc/passwd" → attacker trying to escape uploads dir., done by normalize
            if (filename == null || filename.isBlank()) {
                throw new FileStorageException("Filename cannot be empty");
            }

            // Step 2: resolve and normalize the full path
            Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
            Path filePath   = uploadPath.resolve(filename).normalize();

            // Step 3: confirm resolved path still starts with uploadDirectory
            //         if attacker passed "../secret.txt", normalize() would resolve
            //         it outside uploadDir — this check catches that
            if (!filePath.startsWith(uploadPath)) {
                throw new FileStorageException("Access denied: invalid file path");
            }

            // Step 4: build resource and verify it exists
            Resource resource = new UrlResource(filePath.toUri()); //as this works with Uniform Resource Identifier 
            //.toUri() converts that file path into a URI (like file:///C:/folder/file.txt).
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException("File not found or not readable: " + filename);
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new FileStorageException("Malformed file path for: " + filename);
        }
    }


    public boolean deleteIfExists(String filename) {
    	
    	if(filename ==null)
    		return true;
        try {
            // ── PATH TRAVERSAL PROTECTION (same pattern) ───────────
            Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
            Path filePath   = uploadPath.resolve(filename).normalize();//remove the . or . .
            	
            if (!filePath.startsWith(uploadPath)) {
                throw new FileStorageException("Access denied: invalid file path");
            }

            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Issue occurred while trying to locate previous file");
        }
    }
}