package ar.buildrr.feedback.handler;

import lombok.Data;

import java.time.Instant;

@Data
public class ErrorApi {
  private String message;
  private int status;
  private String path;
  private Instant timestamp;
}
