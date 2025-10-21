package ru.sentidas.rangiffler.rest.model;

import ru.sentidas.rangiffler.utils.OAuthUtils;

public record Code(
        String codeVerifier,
        String codeChallenge
) {
    public static Code generate() {
        String verifier = OAuthUtils.generateCodeVerifier();
        String challenge = OAuthUtils.generateCodeChallange(verifier);
        return new Code(verifier, challenge);
    }
}
