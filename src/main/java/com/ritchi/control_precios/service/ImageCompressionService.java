package com.ritchi.control_precios.service;

import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class ImageCompressionService {

    public String compressAndEncodeImage(byte[] imageBytes, int maxWidth, int maxHeight, float quality) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (originalImage == null) {
            throw new IOException("No se pudo leer la imagen");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double scale = Math.min(
            (double) maxWidth / originalWidth,
            (double) maxHeight / originalHeight
        );

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        BufferedImage resizedImage = new BufferedImage(
            newWidth,
            newHeight,
            BufferedImage.TYPE_INT_RGB
        );

        java.awt.Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", baos);

        byte[] compressedBytes = baos.toByteArray();
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(compressedBytes);
    }

    public String compressAndEncodeImage(byte[] imageBytes) throws IOException {
        return compressAndEncodeImage(imageBytes, 800, 600, 0.85f);
    }

    public byte[] compressImage(byte[] imageBytes, int maxWidth, int maxHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        
        if (originalImage == null) {
            throw new IOException("No se pudo leer la imagen");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        double scale = Math.min(
            (double) maxWidth / originalWidth,
            (double) maxHeight / originalHeight
        );
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        BufferedImage resizedImage = new BufferedImage(
            newWidth, 
            newHeight, 
            BufferedImage.TYPE_INT_RGB
        );
        
        java.awt.Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", baos);
        
        return baos.toByteArray();
    }
}