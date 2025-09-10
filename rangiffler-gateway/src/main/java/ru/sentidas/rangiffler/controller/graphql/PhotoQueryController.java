//package guru.qa.rangiffler.controller.graphql;
//
//import guru.qa.rangiffler.model.Photo;
//import guru.qa.rangiffler.service.PhotoService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.QueryMapping;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//
//
//@Controller
//@PreAuthorize("isAuthenticated()")
//public class PhotoQueryController {
//
//    private final PhotoService photoService;
//
//
//    @Autowired
//    public PhotoQueryController(PhotoService photoService) {
//        this.photoService = photoService;
//    }
//
//
//    @QueryMapping
//    public Page<Photo> photos(@Argument int page, @Argument int size) {
//        return photoService.getPhotos(page, size);
//    }
//}
