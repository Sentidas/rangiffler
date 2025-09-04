package ru.sentidas.rangiffler.ex;

public class SameUsernameException extends RuntimeException {
  public SameUsernameException(String message) {
    super(message);
  }
}
