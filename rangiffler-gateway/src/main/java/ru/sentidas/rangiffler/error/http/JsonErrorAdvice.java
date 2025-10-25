package ru.sentidas.rangiffler.error.http;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.sentidas.rangiffler.InvalidImageFormatException;


import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Глобальный обработчик HTTP-ошибок:
 * формирует JSON для типовых исключений (400, 415).
 */
@ControllerAdvice
public class JsonErrorAdvice {

    private static final Pattern MAX_STR_LEN = Pattern.compile("maximum allowed \\((\\d+)");

    // 400, превышение лимита стандартной настройки Jackson (StreamReadConstraints)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handle(HttpMessageNotReadableException ex) {
        String msg = ex.getMessage();
        if (msg != null && msg.contains("StreamReadConstraints.getMaxStringLength")) {
            long limitBytes = extractLimitBytes(msg);
            long limitMb = Math.max(1, limitBytes / (1024 * 1024));
            Map<String, Object> body = Map.of(
                    "error", "PAYLOAD_TOO_LARGE_STRING",
                    "message", "Limit " + limitMb + " MB",
                    "limitBytes", limitBytes
            );
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        }
        throw ex;
    }

    // 415, формат картинки не из разрешённых
    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidImage(InvalidImageFormatException ex) {
        Map<String, Object> body = (ex.getAllowed() != null)
                ? Map.of(
                "error", "UNSUPPORTED_IMAGE_FORMAT",
                "message", "Unsupported image format. Allowed: jpg, png, gif, webp",
                "allowed", List.copyOf(ex.getAllowed())
        )
                : Map.of(
                "error", "UNSUPPORTED_IMAGE_FORMAT",
                "message", "Unsupported image format"
        );

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private long extractLimitBytes(String msg) {
        Matcher m = MAX_STR_LEN.matcher(msg);
        if (m.find()) {
            try { return Long.parseLong(m.group(1)); } catch (NumberFormatException ignored) {}
        }
        return 20_000_000L;
    }
}
