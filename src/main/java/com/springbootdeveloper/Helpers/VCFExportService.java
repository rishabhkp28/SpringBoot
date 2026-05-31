package com.springbootdeveloper.Helpers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.springbootdeveloper.DTO.ContactDto;

@Service
public class VCFExportService {

    private final FileHandler fileHandler;
    

    public VCFExportService(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public byte[] exportUserContacts(List<ContactDto> contacts) {

        StringBuilder sb = new StringBuilder();

        for (ContactDto contact : contacts) {

            sb.append("BEGIN:VCARD\n");
            sb.append("VERSION:3.0\n");

            if (contact.getName() != null) {
                sb.append("FN:")
                  .append(contact.getName())
                  .append("\n");
            }

            if (contact.getPhone() != null) {
                sb.append("TEL:")
                  .append(contact.getPhone())
                  .append("\n");
            }

            if (contact.getEmail() != null) {
                sb.append("EMAIL:")
                  .append(contact.getEmail())
                  .append("\n");
            }

            appendPhoto(contact, sb);

            sb.append("END:VCARD\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendPhoto(ContactDto contact,
                             StringBuilder sb) {

        try {

            String imageName = contact.getFileName();

            if (imageName == null || imageName.isBlank()) {
                return;
            }

            Resource resource = fileHandler.getFile(imageName);

            byte[] imageBytes =
                    resource.getInputStream()
                            .readAllBytes();

            String base64 =
                    Base64.getEncoder()
                          .encodeToString(imageBytes); //changed to 64 encoding as vcf supports text only

            String imageType =
                    getImageType(imageName);	

            sb.append("PHOTO;ENCODING=b;TYPE=")
              .append(imageType)
              .append(":")
              .append(base64)
              .append("\n");

        } catch (IOException ex) {

            System.err.println("File Hasnt been found , get lost");

        } catch (Exception ex) {

        	 System.err.println("We have reached the second exception point");

        }
    }

    private String getImageType(String filename) {

        String lower = filename.toLowerCase();

        if (lower.endsWith(".png")) {
            return "PNG";
        }

        if (lower.endsWith(".gif")) {
            return "GIF";
        }

        return "JPEG";
    }
}