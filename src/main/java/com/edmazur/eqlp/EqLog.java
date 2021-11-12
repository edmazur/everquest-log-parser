package com.edmazur.eqlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.edmazur.eqlp.file.LogSeeker;

/**
 * Entry point for EQ log processing library.
 */
public class EqLog implements Runnable {

  private static final String LOGS_DIRECTORY = "Logs";

  private static final String BLUE_SUFFIX = "project1999";
  private static final String GREEN_SUFFIX = "P1999Green";
  private static final String RED_SUFFIX = "P1999PVP";

  private final Path eqInstallDirectory;
  private final ZoneId timezone;
  private final String server;
  private final String character;
  private final Instant parseStart;
  private final Instant parseEnd;

  private List<EqLogListener> eqLogListeners = new ArrayList<EqLogListener>();

  /**
   * Creates a runnable EQ log reader.
   *
   * @param eqInstallDirectory Path to where EQ is installed.
   * @param timezone Timezone to interpret log timestamps in.
   * @param server One of "blue", "red", or "green".
   * @param character Character name.
   * @param parseStart Timestamp to start parsing at. Set to Instant.MIN to read
   *     from start of file.
   * @param parseEnd Timestamp to stop parsing at. Set to Instant.Now() to stop
   *     at end of file. Set to Instant.MAX to read continuously after reaching
   *     end of file.
   */
  public EqLog(
      Path eqInstallDirectory,
      ZoneId timezone,
      String server,
      String character,
      Instant parseStart,
      Instant parseEnd) {
    this.eqInstallDirectory = eqInstallDirectory;
    this.timezone = timezone;
    this.server = server;
    this.character = character;
    this.parseStart = parseStart;
    this.parseEnd = parseEnd;
  }

  /**
   * Adds a listener. It will be called with each parsed event.
   *
   * @param eqLogListener The listener to add.
   */
  public void addListener(EqLogListener eqLogListener) {
    eqLogListeners.add(eqLogListener);
  }

  @Override
  public void run() {
    // TODO: Add unit tests for this logic once its structure feels more
    // finalized.
    File eqLogFile = eqInstallDirectory
        .resolve(LOGS_DIRECTORY)
        .resolve(getLogFileName(server, character))
        .toFile();
    BufferedReader bufferedReader = null;
    try {
      bufferedReader =
          new LogSeeker(
              eqLogFile,
              EqLogEvent.LINE_PATTERN,
              EqLogEvent.TIMESTAMP_FORMAT,
              timezone)
              .seek(parseStart);
    } catch (IOException e) {
      // TODO: Handle this more gracefully.
      e.printStackTrace();
      System.exit(-1);
    }
    while (true) {
      String line = null;
      try {
        line = bufferedReader.readLine();
      } catch (IOException e) {
        // TODO: Handle this more gracefully.
        e.printStackTrace();
        System.exit(-1);
      }
      Optional<EqLogEvent> maybeEqLogEvent = EqLogEvent.parseFromLine(line);
      if (maybeEqLogEvent.isEmpty()) {
        continue;
      }
      EqLogEvent eqLogEvent = maybeEqLogEvent.get();
      Instant timestamp =
          eqLogEvent.getTimestamp().atZone(timezone).toInstant();
      if (timestamp.isAfter(parseEnd)) {
        break;
      }
      for (EqLogListener eqLogListener : eqLogListeners) {
        eqLogListener.onEvent(eqLogEvent);
      }
    }
  }

  private String getLogFileName(String server, String character) {
    return "eqlog_" + character + "_" + getLogFileSuffix(server) + ".txt";
  }

  private String getLogFileSuffix(String server) {
    switch (server) {
      case "blue":
        return BLUE_SUFFIX;
      case "green":
        return GREEN_SUFFIX;
      case "red":
        return RED_SUFFIX;
      default:
        throw new IllegalArgumentException(
            "Unknown log file suffix for server " + server);
    }
  }

}