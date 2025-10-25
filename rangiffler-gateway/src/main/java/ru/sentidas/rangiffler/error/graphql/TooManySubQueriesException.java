package ru.sentidas.rangiffler.error.graphql;

public class TooManySubQueriesException extends RuntimeException {
  public TooManySubQueriesException(String message) {
    super(message);
  }
}
