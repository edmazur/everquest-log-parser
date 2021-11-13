package com.edmazur.eqlp.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parent class for log file search implementations.
 */
// TODO: Consider adding binary search or interpolation search. Jump search is
// pretty fast (~2 seconds to reach the end of a ~750MB file). Other algorithms
// would probably be even faster, but more annoying to implement.
public abstract class LogSeeker {

  // The length of the longest supported line. Important for implementations
  // that need lookback to be able to set buffer sizes.
  // Currently set to well above the length of the longest line in the log of
  // what I plan to use this generic utility for (EQ log parsing, Telin
  // Darkforest epic spam is 802 characters).
  protected static int LONGEST_SUPPORTED_LINE = 4096;

  protected final File log;
  // TODO: Generalize this further by replacing the pattern, formatter, and
  // timezone with a Function<String, Comparable> transformer.
  protected final Pattern timestampExtractor;
  protected final DateTimeFormatter timestampFormatter;
  protected final ZoneId timezone;

  public LogSeeker(
      File log,
      Pattern timestampExtractor,
      DateTimeFormatter timestampFormatter,
      ZoneId timezone) {
    this.log = log;
    this.timestampExtractor = timestampExtractor;
    this.timestampFormatter = timestampFormatter;
    this.timezone = timezone;
  }

  public abstract BufferedReader seek(Instant seekStart) throws IOException;

  public abstract String getStrategyDescription();

  protected Optional<Instant> getInstant(String line) {
    Matcher matcher = timestampExtractor.matcher(line);
    if (!matcher.matches() || matcher.groupCount() != 2) {
      return Optional.empty();
    }
    LocalDateTime timestamp = null;
    try {
      timestamp = LocalDateTime.parse(matcher.group(1), timestampFormatter);
    } catch (DateTimeParseException e) {
      return Optional.empty();
    }
    return Optional.of(timestamp.atZone(timezone).toInstant());
  }

}