package com.edmazur.eqlp.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogSeeker {

  // This (I think? not fully tested) needs to be larger than the longest line.
  // As of November 2021, the longest line of what I plan to use this generic
  // utility for (EQ log parsing) is 802 characters (Telin Darkforest epic
  // spam).
  private static int LOOKAHEAD_BUFFER_SIZE = 4096;

  private final File log;
  private final Pattern timestampExtractor;
  private final DateTimeFormatter timestampFormatter;
  private final ZoneId timezone;

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

  public BufferedReader seek(Instant seekStart) throws IOException {
    // TODO: Improve performance with binary search. This is extremely slow if
    // you need to seek far into a large file.
    BufferedReader bufferedReader = null;
    try {
      bufferedReader = new BufferedReader(new FileReader(log));
    } catch (FileNotFoundException e) {
      // TODO: Handle this more gracefully.
      e.printStackTrace();
      System.exit(-1);
    }
    while (true) {
      String line;
      bufferedReader.mark(LOOKAHEAD_BUFFER_SIZE);

      // If we reach the end of the file without finding an entry past
      // seekStart, then return the buffer
      if ((line = bufferedReader.readLine()) == null) {
        // Reached the end of the file without finding an entry past seekStart.
        return bufferedReader;
      }

      Optional<Instant> maybeTimestamp = getInstant(line);
      if (maybeTimestamp.isEmpty()) {
        continue;
      }
      Instant timestamp = maybeTimestamp.get();
      if (!timestamp.isBefore(seekStart)) {
        bufferedReader.reset();
        return bufferedReader;
      }
    }
  }

  private Optional<Instant> getInstant(String line) {
    Matcher matcher = timestampExtractor.matcher(line);
    if (!matcher.matches() || matcher.groupCount() != 2) {
      return Optional.empty();
    }
    LocalDateTime timestamp =
        LocalDateTime.parse(matcher.group(1), timestampFormatter);
    return Optional.of(timestamp.atZone(timezone).toInstant());
  }

}