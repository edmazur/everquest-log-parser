package com.edmazur.eqlp.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implements linear search for searching through a log file.
 */
public class LinearSearchLogSeeker extends LogSeeker {

  public LinearSearchLogSeeker(
      File log,
      Pattern timestampExtractor,
      DateTimeFormatter timestampFormatter,
      ZoneId timezone) {
    super(log, timestampExtractor, timestampFormatter, timezone);
  }

  @Override
  public BufferedReader seek(Instant seekStart) throws IOException {
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
      bufferedReader.mark(LONGEST_SUPPORTED_LINE);

      // If we reach the end of the file without finding an entry past
      // seekStart, then return the buffer.
      if ((line = bufferedReader.readLine()) == null) {
        return bufferedReader;
      }

      Optional<Instant> maybeTimestamp = getInstant(line);
      if (maybeTimestamp.isEmpty()) {
        continue;
      }
      Instant timestamp = maybeTimestamp.get();
      if (!seekStart.isAfter(timestamp)) {
        bufferedReader.reset();
        return bufferedReader;
      }
    }
  }

  @Override
  public String getStrategyDescription() {
    return "Linear search";
  }

}
