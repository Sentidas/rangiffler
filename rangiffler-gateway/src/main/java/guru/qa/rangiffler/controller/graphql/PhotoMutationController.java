package guru.qa.rangiffler.controller.graphql;

import guru.qa.rangiffler.model.Photo;
import guru.qa.rangiffler.model.input.PhotoInput;
import guru.qa.rangiffler.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.UUID;


@Controller
@PreAuthorize("isAuthenticated()")
public class PhotoMutationController {

    private final PhotoService photoService;


    @Autowired
    public PhotoMutationController(PhotoService photoService) {
        this.photoService = photoService;
    }


    @MutationMapping
    public Photo photo(@AuthenticationPrincipal Jwt principal,
                       @Argument PhotoInput input) {
        String username = principal.getClaim("sub");

         if (input.id() != null) {
            return photoService.updatePhoto(username, input);
        }
        else if (input.id() == null) {
            return photoService.createPhoto(username, input);
        }
        else throw new IllegalArgumentException("PhotoInput is invalid");
    }

    @MutationMapping
    public Boolean deletePhoto(@AuthenticationPrincipal Jwt principal,
                               @Argument UUID id) {
        String username = principal.getClaim("sub");
        return photoService.deletePhoto(username, id);
    }

}
