package com.springbootdeveloper.Helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.springbootdeveloper.Exceptions.EmptyFileException;
import com.springbootdeveloper.Exceptions.FileSizeException;
import com.springbootdeveloper.Exceptions.FileStorageException;
import com.springbootdeveloper.Exceptions.UnsupportedFileTypeException;

@Component
public class FileUploader 
{
	
	private static final String uploadDirectory = "C:\\PLACEMENTS\\JAVA\\Java Developer\\SmartContactManager\\src\\main\\resources\\static\\images";
	private static final long minSize = 10 * 1024;      // 10 KB
	private static final long maxSize = 2 * 1024 * 1024; // 2 MB	
	
	
	
	public String upload(MultipartFile multipartFile) {

	    if (multipartFile.isEmpty()) {  //validating the existence of the file
	        throw new EmptyFileException("File cannot be empty");
	    }
	    
	    	final long fileSize = multipartFile.getSize();
	   	    
	    
	    if(fileSize > maxSize || fileSize < minSize)
	    {
	    	throw new FileSizeException("File size must be between 10KB and 2MB");
	    }
	    
	    	
	    
	    
	    
	    
	    String contentType = multipartFile.getContentType();
	    
	    if(!contentType.startsWith("image/"))
	    {
	    	throw new UnsupportedFileTypeException("File should be an image");
	    }
	    
	    
	    
	   
	    // 4️ Generating a completely unique filename with proper extension
	    
	    
        String extension = ".jpg"; // default
        if (contentType.equals("image/png")) {
            extension = ".png";
        } else if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) {
            extension = ".jpg";
        } else if (contentType.equals("image/gif")) {
            extension = ".gif";
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
	    
	    Path targetPath = Paths.get(uploadDirectory, uniqueFilename);

	    try (InputStream inputStream = multipartFile.getInputStream()) {
	        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
	    } catch (IOException e) {
	        throw new FileStorageException("Issue occurred while trying to save the file", e);
	    }
	    
	    return uniqueFilename;
	}
	
	
}
