package ru.manager.ProgectManager.statics;

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

public class PhotoCompressor {
    public static byte[] compress(byte[] input, String extension) {
        try {
            InputStream inputStream = new ByteArrayInputStream(input);
            BufferedImage image = ImageIO.read(inputStream);
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            ImageWriter writer = ImageIO
                    .getImageWritersByFormatName(extension.substring(extension.indexOf('.')))
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
        } catch (IOException e){
            return input;
        }
    }
}
