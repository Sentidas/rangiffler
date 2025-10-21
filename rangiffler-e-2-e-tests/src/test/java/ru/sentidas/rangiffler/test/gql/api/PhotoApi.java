package ru.sentidas.rangiffler.test.gql.api;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.rx2.Rx2Apollo;
import ru.sentidas.*;
import ru.sentidas.rangiffler.utils.ImageHelper;
import ru.sentidas.type.*;

import java.util.UUID;

public class PhotoApi {

    private final ApolloClient apollo;

    public PhotoApi(ApolloClient apollo) {
        this.apollo = apollo;
    }

    public CreatePhotoMutation.Data createPhoto(String token, String src, String description, String countryCode) {
        PhotoInput input = PhotoInput.builder()
                .src(src)
                .description(description)
                .country(CountryInput.builder().code(countryCode).build())
                .build();

        CreatePhotoMutation mutation = CreatePhotoMutation.builder().input(input).build();

        ApolloCall<CreatePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);

        ApolloResponse<CreatePhotoMutation.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }

    public UpdatePhotoMutation.Data updatePhoto(String token, String id, String src, String description, String countryCode) {
        PhotoInput.Builder b = PhotoInput.builder().id(id);
        if (src != null) b.src(src);
        if (description != null) b.description(description);
        if (countryCode != null) b.country(CountryInput.builder().code(countryCode).build());

        UpdatePhotoMutation mutation = UpdatePhotoMutation.builder().input(b.build()).build();
        ApolloCall<UpdatePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);

        ApolloResponse<UpdatePhotoMutation.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }


    public UpdatePhotoMutation.Data updatePhotoFromClasspath(String token, String id, String resourcePath, String description, String countryCode) {
        String src = ImageHelper.fromClasspath(resourcePath).toDataUrl();
        PhotoInput.Builder b = PhotoInput.builder().id(id);
        b.src(src);
        if (description != null) b.description(description);
        if (countryCode != null) b.country(CountryInput.builder().code(countryCode).build());

        UpdatePhotoMutation mutation = UpdatePhotoMutation.builder().input(b.build()).build();
        ApolloCall<UpdatePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);

        ApolloResponse<UpdatePhotoMutation.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }

    public ApolloResponse<UpdatePhotoMutation.Data> tryUpdatePhoto(String token, String id, String src, String description, String countryCode) {
        PhotoInput.Builder b = PhotoInput.builder().id(id);
        if (src != null) b.src(src);
        if (description != null) b.description(description);
        if (countryCode != null) b.country(CountryInput.builder().code(countryCode).build());

        UpdatePhotoMutation mutation = UpdatePhotoMutation.builder().input(b.build()).build();
        ApolloCall<UpdatePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);

        return Rx2Apollo.single(call).blockingGet();

    }

    public ApolloResponse<UpdatePhotoMutation.Data> tryUpdatePhotoWithoutAuth(String id, String src, String description, String countryCode) {
        PhotoInput.Builder b = PhotoInput.builder().id(id);
        if (src != null) b.src(src);
        if (description != null) b.description(description);
        if (countryCode != null) b.country(CountryInput.builder().code(countryCode).build());

        UpdatePhotoMutation mutation = UpdatePhotoMutation.builder().input(b.build()).build();
        ApolloCall<UpdatePhotoMutation.Data> call = apollo.mutation(mutation);

        return Rx2Apollo.single(call).blockingGet();

    }

    public DeletePhotoMutation.Data deletePhoto(String token, String id) {
        DeletePhotoMutation mutation = DeletePhotoMutation.builder().id(id).build();
        ApolloCall<DeletePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);
        ApolloResponse<DeletePhotoMutation.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }

    public DeletePhotoMutation.Data deletePhoto(String token, UUID id) {
        return deletePhoto(token, id.toString());
    }

    public ApolloResponse<DeletePhotoMutation.Data> tryDeletePhoto(String token, String id) {
        DeletePhotoMutation mutation = DeletePhotoMutation.builder().id(id).build();
        ApolloCall<DeletePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);
        return Rx2Apollo.single(call).blockingGet();
    }

    public ApolloResponse<DeletePhotoMutation.Data> tryDeletePhotoWithoutAuth(String id) {
        DeletePhotoMutation mutation = DeletePhotoMutation.builder().id(id).build();
        ApolloCall<DeletePhotoMutation.Data> call = apollo.mutation(mutation);
        return Rx2Apollo.single(call).blockingGet();
    }

    public ApolloResponse<DeletePhotoMutation.Data> tryDeletePhoto(String token, UUID id) {
       return tryDeletePhoto(token, id.toString());
    }

    public LikePhotoMutation.Data likePhoto(String token, String id, String likerId) {
        PhotoInput input = PhotoInput.builder()
                .id(id)
                .like(LikeInput.builder().user(likerId).build())
                .build();
        LikePhotoMutation mutation = LikePhotoMutation.builder().input(input).build();
        ApolloCall<LikePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);
        ApolloResponse<LikePhotoMutation.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }
}
