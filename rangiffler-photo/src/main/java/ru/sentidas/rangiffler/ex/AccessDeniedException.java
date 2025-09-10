package ru.sentidas.rangiffler.ex;

public class AccessDeniedException extends RuntimeException {
  public AccessDeniedException(String message) { super(message); }
}