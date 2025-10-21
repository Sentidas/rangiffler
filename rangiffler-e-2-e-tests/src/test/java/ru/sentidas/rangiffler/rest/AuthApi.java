package ru.sentidas.rangiffler.rest;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface AuthApi {

    @GET("oauth2/authorize")
    Call<Void> authorize(@Query("response_type") String responseType,
                         @Query("client_id") String clientId,
                         @Query("scope") String scope,
                         @Query(value = "redirect_uri", encoded = true) String redirectUri,
                         @Query("code_challenge") String codeChallenge,
                         @Query("code_challenge_method") String codeChallengeMethod
    );

    @POST("login")
    @FormUrlEncoded
    Call<Void> login(@Field("username") String username,
                     @Field("password") String password,
                     @Field("_csrf") String csrf
    );

    @GET("login?error")
    Call<ResponseBody> requestLoginForm();

    @POST("oauth2/token")
    @FormUrlEncoded
    Call<JsonNode> token(@Field("code") String code,
                         @Field(value = "redirect_uri", encoded = true) String redirectUri,
                         @Field("client_id") String clientId,
                         @Field("code_verifier") String codeVerifier,
                         @Field("grant_type") String grantType
    );

    @POST("register")
    @FormUrlEncoded
    Call<ResponseBody> register(
            @Field("username") String username,
            @Field("password") String password,
            @Field("passwordSubmit") String passwordSubmit,
            @Field("_csrf") String csrf);


    @GET("register")
    Call<ResponseBody> requestRegisterForm();

    @GET("logout")
    @FormUrlEncoded
    Call<Void> logout();
}
