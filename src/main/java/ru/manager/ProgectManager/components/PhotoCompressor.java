package ru.manager.ProgectManager.components;

import lombok.extern.java.Log;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.enums.Size;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@Log
public class PhotoCompressor {
    public byte[] compress(MultipartFile file, Size size) throws IOException{
        if (file.getOriginalFilename() == null) {
            return null;
        }
        BufferedImage image = ImageIO.read(file.getInputStream());
        image = (size == Size.LARGE
                ? Scalr.resize(image, 1920, 1080)
                : Scalr.resize(image, size == Size.MIDDLE ? 600 : 200));
        if (image.getColorModel().hasAlpha()) {
            BufferedImage tempImage = new BufferedImage(image.getWidth(), image.getHeight(),
                    BufferedImage.TYPE_INT_BGR);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    tempImage.setRGB(x, y, new Color(image.getRGB(x, y), false).getRGB());
                }
            }
            image = tempImage;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ImageWriter writer = ImageIO
                .getImageWritersByFormatName("jpg")
                .next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);

        int inputSize = image.getHeight() * image.getWidth() * 3;
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(inputSize < 204_800 ? 1 : 204_800f / inputSize);  // Change the quality value you prefer
        writer.write(null, new IIOImage(image, null, null), param);

        byte[] result = os.toByteArray();

        os.close();
        ios.close();
        writer.dispose();

        return result;
    }
}
