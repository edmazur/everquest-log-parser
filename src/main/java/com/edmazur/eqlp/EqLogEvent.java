package com.edmazur.eqlp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an individual EQ log event corresponding to a single log line.
 *
 * TODO: Decide whether to push timezone logic down into this class.
 */
public class EqLogEvent {

  public static final Pattern LINE_PATTERN =
      Pattern.compile("^\\[(.+?)\\] (.+)$");
  public static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");

  // Example: "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'hi'"
  private final String fullLine;
  // Example: LocalDateTime representation of "Fri Oct 25 18:58:06 2019"
  private final LocalDateTime timestamp;
  // Example: "Stanvern says out of character, 'hi'"
  private final String payload;

  private EqLogEvent(String fullLine, LocalDateTime timestamp, String payload) {
    this.fullLine = fullLine;
    this.timestamp = timestamp;
    this.payload = payload;
  }

  /**
   * Parses an EQ log event from a single log line.
   *
   * @param line The line to parse.
   * @return The parsed line, or Optional.empty() if the input could not be
   *     parsed.
   */
  public static Optional<EqLogEvent> parseFromLine(String line) {
    // TODO: Make this more robust against bad input.
    Matcher matcher = LINE_PATTERN.matcher(line);
    if (matcher.matches() && matcher.groupCount() == 2) {
      LocalDateTime timestamp = null;
      try {
        timestamp = LocalDateTime.parse(matcher.group(1), TIMESTAMP_FORMAT);
      } catch (DateTimeParseException e) {
        // TODO: Handle this more gracefully.
        return Optional.empty();
      }
      String payload = matcher.group(2);
      return Optional.of(new EqLogEvent(line, timestamp, payload));
    } else {
      // TODO: Handle this more gracefully.
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    return fullLine;
  }

  /**
   * @return The full line.
   */
  public String getFullLine() {
    return fullLine;
  }

  /**
   * @return The timestamp.
   */
  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  /**
   * @return The payload.
   */
  public String getPayload() {
    return payload;
  }

}