package ru.sentidas.rangiffler;

import java.util.Base64;
import java.util.Locale;

public record DataUrl(String mime, byte[] bytes) {

    private static final String PREFIX = "data:";
    private static final String BASE64 = ";base64,";

    public static DataUrl parse(String dataUrl) {
        if (dataUrl == null || !dataUrl.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Not a data: URL");
        }
        int semi = dataUrl.indexOf(BASE64);
        if (semi < 0) {
            throw new IllegalArgumentException("data: URL must contain ';base64,'");
        }
        String mime = dataUrl.substring(PREFIX.length(), semi).trim().toLowerCase(Locale.ROOT);
        String b64 = dataUrl.substring(semi + BASE64.length());
        byte[] bytes = Base64.getDecoder().decode(b64);
        return new DataUrl(mime, bytes);
    }

    public static String build(String mime, byte[] bytes) {
        if (mime == null || mime.isBlank()) {
            throw new IllegalArgumentException("MIME must be provided for DataUrl.build");
        }
        String b64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mime + BASE64 + b64;
    }
}
