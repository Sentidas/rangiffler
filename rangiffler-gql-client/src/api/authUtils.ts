import * as crypto from "crypto-js";
import sha256 from "crypto-js/sha256";
import Base64 from "crypto-js/enc-base64";
import {authClient} from "./authClient.ts";

const base64Url = (str: string | crypto.lib.WordArray) => {
    return str.toString(Base64).replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
}

const generateCodeVerifier = () => {
    return base64Url(crypto.enc.Base64.stringify(crypto.lib.WordArray.random(32)));
}

const generateCodeChallenge = () => {
    const codeVerifier = localStorage.getItem("codeVerifier");
    return base64Url(sha256(codeVerifier!));
}

const getAuthLink = (codeChallenge: string) => {
    return `${import.meta.env.VITE_AUTH_URL}/oauth2/authorize?response_type=${import.meta.env.VITE_RESPONSE_TYPE}&client_id=${import.meta.env.VITE_CLIENT_ID}&scope=openid&redirect_uri=${import.meta.env.VITE_FRONT_URL}/authorized&code_challenge=${codeChallenge}&code_challenge_method=S256`
}

const getTokenFromUrlEncodedParams = (code: string, verifier: string) => {
    return new URLSearchParams({
        "code": code,
        "redirect_uri": `${import.meta.env.VITE_FRONT_URL}/authorized`,
        "code_verifier": verifier,
        "grant_type": "authorization_code",
        "client_id": `${import.meta.env.VITE_CLIENT_ID}`,
    });
}

const getRefreshTokenFromUrlEncodedParams = (refreshToken: string) => {
    return new URLSearchParams({
        "grant_type": "refresh_token",
        "refresh_token": refreshToken,
    });
}

const refreshToken = async () => {
    const refreshToken = localStorage.getItem("refresh_token");
    if (refreshToken === null) {
        return;
    }
    const data = getRefreshTokenFromUrlEncodedParams(refreshToken);
    try {
        const res = await authClient.refreshToken("oauth2/token", data);
        if (res?.id_token) {
            localStorage.setItem("id_token", res.id_token);
            localStorage.setItem("refresh_token", res.refresh_token);
        }
    } catch (err) {
        clearSession();
    }
}

const initLocalStorageAndRedirectToAuth = () => {
    const codeVerifier = generateCodeVerifier();
    localStorage.setItem('codeVerifier', codeVerifier);
    const codeChallenge = generateCodeChallenge();
    localStorage.setItem('codeChallenge', codeChallenge);

    const link = getAuthLink(codeChallenge);
    window.location.replace(link);
}

const clearSession = () => {
    localStorage.removeItem('codeVerifier');
    localStorage.removeItem('codeChallenge');
    localStorage.removeItem('id_token');
    localStorage.removeItem('refresh_token');
}

export {generateCodeChallenge, generateCodeVerifier, getAuthLink, getTokenFromUrlEncodedParams, clearSession, initLocalStorageAndRedirectToAuth, refreshToken};