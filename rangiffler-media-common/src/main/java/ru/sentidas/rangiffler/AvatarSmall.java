package ru.sentidas.rangiffler;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Единый генератор уменьшенной версии аватара (PNG size×size)
 */
public final class AvatarSmall {

    private AvatarSmall() {}

    /**
     * Уменьшает исходные байты изображения до квадратного PNG size×size.
     * Без проверок форматов и лимитов.
     */
    public static byte[] fromBytes(byte[] original, int size) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(original);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            BufferedImage src = ImageIO.read(in);
            if (src == null) {
                throw new RuntimeException("Unsupported image content");
            }

            Thumbnails.of(src)
                    .size(size, size)
                    .outputFormat("png")
                    .toOutputStream(out);

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Small avatar generation failed", e);
        }
    }

    /**
     * data:image/... → уменьшенный PNG size×size.
     * Без проверок форматов и лимитов.
     */
    public static byte[] fromDataUrl(String dataUrl, int size) {
        byte[] original = DataUrl.parse(dataUrl).bytes();
        return fromBytes(original, size);
    }
}
