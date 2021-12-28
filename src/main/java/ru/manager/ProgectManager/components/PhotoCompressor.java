package ru.manager.ProgectManager.components;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class PhotoCompressor {
    public byte[] compress(MultipartFile input) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        BufferedImage image = ImageIO.read(inputStream);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ImageWriter writer = ImageIO
                .getImageWritersByFormatName(input.getName().substring(input.getName().indexOf('.')))
                .next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.125f);  // Change the quality value you prefer
        writer.write(null, new IIOImage(image, null, null), param);

        os.close();
        ios.close();
        writer.dispose();
        return os.toByteArray();
    }
}
