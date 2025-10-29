package ru.sentidas.rangiffler.test.gql.api;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.rx2.Rx2Apollo;
import io.qameta.allure.Step;
import ru.sentidas.CreatePhotoMutation;
import ru.sentidas.DeletePhotoMutation;
import ru.sentidas.LikePhotoMutation;
import ru.sentidas.UpdatePhotoMutation;
import ru.sentidas.rangiffler.utils.ImageHelper;
import ru.sentidas.type.CountryInput;
import ru.sentidas.type.LikeInput;
import ru.sentidas.type.PhotoInput;

import java.util.UUID;

public class PhotoApi {

    private final ApolloClient apollo;

    public PhotoApi(ApolloClient apollo) {
        this.apollo = apollo;
    }

    @Step("GQL CreatePhoto: country={countryCode}")
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

   // @Step("GQL UpdatePhoto: id={id}, resource={resourcePath}, country={countryCode}")
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

    @Step("GQL UpdatePhoto (from classpath): id={id}, resource={resourcePath}, country={countryCode}")
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

    @Step("GQL UpdatePhoto raw: id={id}, country={countryCode}")
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

    @Step("GQL UpdatePhoto without auth: id={id}, country={countryCode}")
    public ApolloResponse<UpdatePhotoMutation.Data> tryUpdatePhotoWithoutAuth(String id, String src, String description, String countryCode) {
        PhotoInput.Builder b = PhotoInput.builder().id(id);
        if (src != null) b.src(src);
        if (description != null) b.description(description);
        if (countryCode != null) b.country(CountryInput.builder().code(countryCode).build());

        UpdatePhotoMutation mutation = UpdatePhotoMutation.builder().input(b.build()).build();
        ApolloCall<UpdatePhotoMutation.Data> call = apollo.mutation(mutation);

        return Rx2Apollo.single(call).blockingGet();

    }

    @Step("GQL DeletePhoto: id={id}")
    public DeletePhotoMutation.Data deletePhoto(String token, String id) {
        DeletePhotoMutation mutation = DeletePhotoMutation.builder().id(id).build();
        ApolloCall<DeletePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);
        ApolloResponse<DeletePhotoMutation.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }

    @Step("GQL DeletePhoto: id={id}")
    public DeletePhotoMutation.Data deletePhoto(String token, UUID id) {
        return deletePhoto(token, id.toString());
    }

    @Step("GQL DeletePhoto raw: id={id}")
    public ApolloResponse<DeletePhotoMutation.Data> tryDeletePhoto(String token, String id) {
        DeletePhotoMutation mutation = DeletePhotoMutation.builder().id(id).build();
        ApolloCall<DeletePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);
        return Rx2Apollo.single(call).blockingGet();
    }

    @Step("GQL DeletePhoto without auth: id={id}")
    public ApolloResponse<DeletePhotoMutation.Data> tryDeletePhotoWithoutAuth(String id) {
        DeletePhotoMutation mutation = DeletePhotoMutation.builder().id(id).build();
        ApolloCall<DeletePhotoMutation.Data> call = apollo.mutation(mutation);
        return Rx2Apollo.single(call).blockingGet();
    }

    @Step("GQL DeletePhoto raw: id={id}")
    public ApolloResponse<DeletePhotoMutation.Data> tryDeletePhoto(String token, UUID id) {
        return tryDeletePhoto(token, id.toString());
    }

    @Step("GQL LikePhoto: photoId={id}, likerId={likerId}")
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

    @Step("GQL LikePhoto raw: photoId={photoId}, likerUserId={likerUserId}")
    public ApolloResponse<LikePhotoMutation.Data> tryLikePhotoRaw(String bearerToken, String photoId, String likerUserId) {
        PhotoInput input = PhotoInput.builder()
                .id(photoId)
                .like(LikeInput.builder().user(likerUserId).build())
                .build();

        LikePhotoMutation mutation = LikePhotoMutation.builder()
                .input(input)
                .build();

        ApolloCall<LikePhotoMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", bearerToken);

        return Rx2Apollo.single(call).blockingGet();
    }
}
