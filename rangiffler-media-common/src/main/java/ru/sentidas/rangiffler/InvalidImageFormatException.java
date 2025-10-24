package ru.sentidas.rangiffler;

import java.util.Set;

public class InvalidImageFormatException extends RuntimeException {
    private final Set<String> allowed;

    public InvalidImageFormatException(String msg, Set<String> allowed) {
        super(msg);
        this.allowed = allowed;
    }
    public Set<String> getAllowed() { return allowed; }
}
