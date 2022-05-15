package ru.manager.ProgectManager.components;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@Log
public class PhotoCompressor {
    public byte[] compress(MultipartFile file) {
        log.info("Input file have " + file.getSize() + " bytes");
        if(file.getOriginalFilename() == null) {
            return null;
        }
        try {
            String filename = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf('.') + 1);
            if(file.getSize() < 524_288){
                return file.getBytes();
            }
            BufferedImage image = ImageIO.read(file.getInputStream());
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            ImageWriter writer = ImageIO
                    .getImageWritersByFormatName(filename)
                    .next();

            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(524_288f / file.getSize());  // Change the quality value you prefer
            writer.write(null, new IIOImage(image, null, null), param);

            byte[] result = os.toByteArray();
            log.info("Image compress to " + result.length + " byte.");

            os.close();
            ios.close();
            writer.dispose();

            return result;
        } catch (IOException e){
            return null;
        }
    }
}
