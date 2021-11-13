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

import com.edmazur.eqlp.Logger;

/**
 * Implements jump search for searching through a log file.
 */
public class JumpSearchLogSeeker extends LogSeeker {

  private static final Logger logger = new Logger();

  // This seems like a good default based on LogSeekerBenchmark experimentation
  // on a ~750MB file.
  private static final int DEFAULT_JUMP_SIZE = 1024 * 1024;

  private final int jumpSize;

  public JumpSearchLogSeeker(
      File log,
      Pattern timestampExtractor,
      DateTimeFormatter timestampFormatter,
      ZoneId timezone) {
    this(log, timestampExtractor, timestampFormatter, timezone, DEFAULT_JUMP_SIZE);
  }

  public JumpSearchLogSeeker(
      File log,
      Pattern timestampExtractor,
      DateTimeFormatter timestampFormatter,
      ZoneId timezone,
      int jumpSize) {
    super(log, timestampExtractor, timestampFormatter, timezone);
    this.jumpSize = jumpSize;
  }

  // TODO: Simplify this algorithm. I'm fairly confident of its correctness
  // based on fairly extensive unit tests, but the logic is super hard to follow
  // and it's plausible an edge case was missed.
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
    logger.log("Beginning search");
    while (true) {
      String line;
      bufferedReader.mark(jumpSize + LONGEST_SUPPORTED_LINE);

      // If we reach the end of the file without finding an entry past
      // seekStart, then return the buffer.
      if ((line = bufferedReader.readLine()) == null) {
        logger.log("Reached end of file");
        return bufferedReader;
      }

      // Determine whether the target line is in jump range.

      bufferedReader.skip(jumpSize);
      // Skip possibly-partial line.
      bufferedReader.readLine();

      // Get info about the upper end of the jump range. In most cases, one line
      // is enough, but in edge cases where the next line is blank or malformed,
      // we need to keep going.
      boolean reachedEndOfFile = false;
      Instant timestamp = null;
      while (true) {
        line = bufferedReader.readLine();
        logger.log("Jump search stage: " + line);
        if (line == null) {
          reachedEndOfFile = true;
          break;
        }
        Optional<Instant> maybeTimestamp = getInstant(line);
        if (maybeTimestamp.isPresent()) {
          timestamp = maybeTimestamp.get();
          break;
        }
      }

      if (reachedEndOfFile || !seekStart.isAfter(timestamp)) {
        // Target line is in jump range, rewind and do linear search.
        // TODO: Re-use LinearSearchLogSeeker here somehow instead of
        // essentially repeating its logic.
        logger.log("Target is in jump range, doing linear search");
        bufferedReader.reset();
        while (true) {
          bufferedReader.mark(LONGEST_SUPPORTED_LINE);

          // If we reach the end of the file without finding an entry past
          // seekStart, then return the buffer.
          if ((line = bufferedReader.readLine()) == null) {
            return bufferedReader;
          }
          logger.log("Linear search stage: " + line);

          Optional<Instant> maybeTimestamp = getInstant(line);
          if (maybeTimestamp.isEmpty()) {
            continue;
          }
          timestamp = maybeTimestamp.get();
          if (!seekStart.isAfter(timestamp)) {
            logger.log(
                "Found a line comes before (or is same as) seekStart, "
                + "resetting to line before: " + line);
            bufferedReader.reset();
            return bufferedReader;
          }
          logger.log("Moving forward a line");
        }
      }
    }
  }

  @Override
  public String getStrategyDescription() {
    return "Jump search with jumpSize=" + jumpSize;
  }

}
