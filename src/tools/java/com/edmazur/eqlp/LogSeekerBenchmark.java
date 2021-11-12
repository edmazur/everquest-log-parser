package com.edmazur.eqlp;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import com.edmazur.eqlp.file.LogSeeker;

/**
 * Reports how long it takes to seek to various positions in a large log file.
 */
public class LogSeekerBenchmark {

  // TODO: Figure out a way to not rely on data outside of the project.
  private static final File file = new File(
      "/opt/everquest/EverQuest Project 1999/Logs/eqlog_Stanvern_P1999Green.txt");

  public static void main(String[] args) throws IOException {
    runBenchmark(
        Instant.MIN,
        "Start of file");
    runBenchmark(
        Instant.now().minus(Duration.ofMinutes(5)),
        "5 minutes before end of file");
  }

  private static void runBenchmark(Instant seekStart, String name) throws IOException {
    LogSeeker logSeeker = new LogSeeker(
        file,
        EqLogEvent.LINE_PATTERN,
        EqLogEvent.TIMESTAMP_FORMAT,
        ZoneId.systemDefault());
    Instant start = Instant.now();
    logSeeker.seek(seekStart);
    Instant end = Instant.now();
    System.out.println(
        getHumanReadableFormat(Duration.between(start, end)) + " - \"" + name +
        "\" benchmark");
  }

  // https://stackoverflow.com/a/40487511/192236
  private static String getHumanReadableFormat(Duration duration) {
    return duration.toString()
        .substring(2)
        .replaceAll("(\\d[HMS])(?!$)", "$1 ")
        .toLowerCase();
  }

}