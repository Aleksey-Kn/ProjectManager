package ru.manager.ProgectManager.components;

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
import java.io.InputStream;

@Component
public class PhotoCompressor {
    public byte[] compress(MultipartFile file) {
        if(file.getOriginalFilename() == null) {
            return null;
        }
        try {
            if(file.getSize() < 524_288){
                return file.getBytes();
            }
            BufferedImage image = ImageIO.read(file.getInputStream());
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            ImageWriter writer = ImageIO
                    .getImageWritersByFormatName(file.getOriginalFilename()
                    .substring(file.getOriginalFilename().indexOf('.')))
                    .next();

            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(524_288f / file.getSize());  // Change the quality value you prefer
            writer.write(null, new IIOImage(image, null, null), param);

            os.close();
            ios.close();
            writer.dispose();
            return os.toByteArray();
        } catch (IOException e){
            return null;
        }
    }
}
