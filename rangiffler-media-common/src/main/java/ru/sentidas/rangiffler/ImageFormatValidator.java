package ru.sentidas.rangiffler;

import java.util.Collections;
import java.util.Set;

public class ImageFormatValidator {
    private final Set<String> allowed;

    public ImageFormatValidator(Set<String> allowed) {
        this.allowed = allowed;
    }

    /** Нужен контроллеру для формирования понятной ошибки */
    public Set<String> allowedMime() {
        return Collections.unmodifiableSet(allowed);
    }

    public boolean isDataUrl(String s) {
        try { DataUrl.parse(s); return true; }
        catch (Exception ignore) { return false; }
    }

    public String extractMimeOrThrow(String dataUrl) {
        String mime = DataUrl.parse(dataUrl).mime();
        if (!allowed.contains(mime)) {
            throw new InvalidImageFormatException("Unsupported image format: " + mime, allowed);
        }
        return mime;
    }

    public void validateDataUrlOrThrow(String dataUrl) {
        extractMimeOrThrow(dataUrl); // парсит и проверяет
    }
}
