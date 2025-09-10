package ru.sentidas.rangiffler.controller.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.model.ggl.input.Country;
import ru.sentidas.rangiffler.model.ggl.input.PhotoInput;
import ru.sentidas.rangiffler.service.api.GrpcGeoClient;
import ru.sentidas.rangiffler.service.api.GrpcPhotoClient;

import java.util.UUID;


@Controller
@PreAuthorize("isAuthenticated()")
public class PhotoMutationController {

    private final GrpcPhotoClient grpcPhotoClient;
    private final GrpcGeoClient grpcGeoClient;

    @Autowired
    public PhotoMutationController(GrpcPhotoClient grpcPhotoClient, GrpcGeoClient grpcGeoClient) {
        this.grpcPhotoClient = grpcPhotoClient;
        this.grpcGeoClient = grpcGeoClient;
    }

//    @SchemaMapping(typeName = "Photo", field = "country")
//    public Country country(Photo photo) {
//        return grpcGeoClient.getByCode(photo.countryCode());
//    }


    @MutationMapping
    public Photo photo(@AuthenticationPrincipal Jwt principal,
                       @Argument PhotoInput input) {
        String username = principal.getClaim("sub");

        if (input.id() == null) {
            return grpcPhotoClient.createPhoto(username, input);
        }

        if (input.like() != null) {
            return grpcPhotoClient.toggleLike(username, input.id());
        }

        return grpcPhotoClient.updatePhoto(username, input);
}

@MutationMapping
public Boolean deletePhoto(@AuthenticationPrincipal Jwt principal,
                           @Argument UUID id) {
    String username = principal.getClaim("sub");
    return grpcPhotoClient.deletePhoto(username, id);
}

}
