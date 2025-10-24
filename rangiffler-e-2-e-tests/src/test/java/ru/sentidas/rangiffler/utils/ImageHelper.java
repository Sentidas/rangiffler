package ru.sentidas.rangiffler.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Locale;

/**
 * Нelper для работы с изображениями из classpath.
 * Умеет: загрузить ресурс как байты (fromClasspath) и собрать data URL (toDataUrl).
 * Используется в API-прекондишенах и сервисах, где нужен data:image/...;base64,...
 */
public final class ImageHelper {

    private final String resourcePath;
    private final byte[] bytes;

    private ImageHelper(String resourcePath, byte[] bytes) {
        this.resourcePath = resourcePath;
        this.bytes = bytes;
    }

    public static ImageHelper fromClasspath(String resourcePath) {
        try (InputStream is = currentClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found on classpath: " + resourcePath);
            }
            byte[] data = is.readAllBytes();
            return new ImageHelper(resourcePath, data);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource: " + resourcePath, e);
        }
    }

    public String toDataUrl() {
        String mime = mimeByExtension(extension());
        String b64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mime + ";base64," + b64;
    }

    private static ClassLoader currentClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl != null ? cl : ImageHelper.class.getClassLoader();
    }

    private String extension() {
        String name = resourcePath.toLowerCase(Locale.ROOT);
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1) : "";
    }

    private static String mimeByExtension(String ext) {
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> throw new IllegalArgumentException("Unsupported image extension: " + ext);
        };
    }
}
