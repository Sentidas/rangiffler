package ru.sentidas.rangiffler.controller.mutation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import ru.sentidas.rangiffler.ImageFormatValidator;

import ru.sentidas.rangiffler.InvalidImageFormatException;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.model.input.PhotoInput;
import ru.sentidas.rangiffler.service.api.GrpcPhotoClient;

import java.util.UUID;

@Controller
@PreAuthorize("isAuthenticated()")
public class PhotoMutationController {

    private final GrpcPhotoClient grpcPhotoClient;
    private final ImageFormatValidator imageFormatValidator;

    @Autowired
    public PhotoMutationController(GrpcPhotoClient grpcPhotoClient,  ImageFormatValidator imageFormatValidator) {
        this.grpcPhotoClient = grpcPhotoClient;
        this.imageFormatValidator = imageFormatValidator;
    }

    @MutationMapping
    public Photo photo(@AuthenticationPrincipal Jwt principal,
                       @Argument PhotoInput input) {
        String username = principal.getClaim("sub");

        if (input.id() == null) {
            if (input.src() == null || input.src().isBlank()) {
                throw new InvalidImageFormatException("`src` is required", imageFormatValidator.allowedMime());
            }
            if (!imageFormatValidator.isDataUrl(input.src())) {
                throw new InvalidImageFormatException("`src` must be data URL", imageFormatValidator.allowedMime());
            }
            imageFormatValidator.validateDataUrlOrThrow(input.src());
            return grpcPhotoClient.createPhoto(username, input);
        }

        if (input.like() != null) {
            return grpcPhotoClient.toggleLike(username, input.id());
        }
        PhotoInput updated = input;
        if (input.src() != null && !input.src().isBlank()) {
            if (imageFormatValidator.isDataUrl(input.src())) {
                imageFormatValidator.validateDataUrlOrThrow(input.src());
            } else {
                updated = new PhotoInput(
                        input.id(),
                        null,
                        input.country(),
                        input.description(),
                        input.like()
                );
            }
        }

        return grpcPhotoClient.updatePhoto(username, updated);
    }

    @MutationMapping
    public Boolean deletePhoto(@AuthenticationPrincipal Jwt principal,
                               @Argument UUID id) {
        String username = principal.getClaim("sub");
        return grpcPhotoClient.deletePhoto(username, id);
    }
}
