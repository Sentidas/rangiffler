package ru.sentidas.rangiffler.media;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер раздачи медиафайлов из MinIO по пути /media/**.
 * Проставляет корректный Content-Type и кэширование (immutable, max-age).
 * В случае отсутствия объекта возвращает 404 (NoSuchKey).
 */
@RestController
@RequiredArgsConstructor
public class MediaController {
    private final MinioClient minio;
    @Value("${app.media.bucket}") private String bucket;
    @Value("${app.media.cache-max-age-seconds:31536000}") private long cacheMaxAge;

    @GetMapping("/media/**")
    public void media(HttpServletRequest req, HttpServletResponse resp) {
        String prefix = req.getContextPath() + "/media/";
        String uri = req.getRequestURI();                     // "/media/photos/.../file.png"
        String objectKey = uri.startsWith(prefix) ? uri.substring(prefix.length()) : null;
        if (objectKey == null || objectKey.isBlank()) { resp.setStatus(400); return; }

        try {
            var stat = minio.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            resp.setContentType(stat.contentType() != null ? stat.contentType() : "application/octet-stream");
            resp.setHeader("Cache-Control", "public, max-age=" + cacheMaxAge + ", immutable");

            try (var in = minio.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
                 var out = resp.getOutputStream()) {
                in.transferTo(out);
                out.flush();
            }
        } catch (io.minio.errors.ErrorResponseException e) {
            resp.setStatus("NoSuchKey".equalsIgnoreCase(e.errorResponse().code()) ? 404 : 500);
        } catch (Exception e) {
            resp.setStatus(500);
        }
    }
}
