package ru.sentidas.rangiffler.service;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;

public class SmallAvatar {

    private static final Logger LOG = LoggerFactory.getLogger(SmallAvatar.class);

    private final int height;
    private final int width;
    private final double quality;
    @Nonnull
    private final String outputFormat;
    @Nullable
    private final String avatarDataUrl;

    public SmallAvatar(int height, int width, @Nullable String photo) {
        this(height, width, 1.0, "png", photo);
    }

    public SmallAvatar(int height, int width, @Nonnull String outputFormat, @Nullable String photo) {
        this(height, width, 1.0, outputFormat, photo);
    }

    public SmallAvatar(int height, int width, double quality, @Nonnull String outputFormat, @Nullable String photo) {
        this.height = height;
        this.width = width;
        this.quality = quality;
        this.outputFormat = outputFormat;
        this.avatarDataUrl = photo;
    }

    public @Nullable byte[] bytes() {
        if (avatarDataUrl != null) {
            try {
                String base64Image = avatarDataUrl.split(",")[1];

                try (ByteArrayInputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(base64Image));
                     ByteArrayOutputStream os = new ByteArrayOutputStream()) {

                    net.coobird.thumbnailator.Thumbnails.of(javax.imageio.ImageIO.read(is))
                            .size(width, height)           // сохраняет пропорции в рамке width×height
                            .outputQuality(quality)
                            .outputFormat(outputFormat)
                            .toOutputStream(os);

                    return os.toByteArray();           // ВОЗВРАЩАЕМ RAW-БАЙТЫ КАРТИНКИ
                }
            } catch (Exception e) {
                LOG.error("### Error while resizing photo");
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private @Nonnull byte[] concatArrays(@Nonnull byte[] first, @Nonnull byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
