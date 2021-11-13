package com.edmazur.eqlp;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import com.edmazur.eqlp.file.JumpSearchLogSeeker;
import com.edmazur.eqlp.file.LinearSearchLogSeeker;
import com.edmazur.eqlp.file.LogSeeker;

/**
 * Reports how long it takes to seek to various positions in a large log file
 * using different LogSeeker implementations.
 */
public class LogSeekerBenchmark {

  // TODO: Figure out a way to not rely on data outside of the project.
  private static final File file = new File(
      "/opt/everquest/EverQuest Project 1999/Logs/eqlog_Stanvern_P1999Green.txt");

  public static void main(String[] args) throws IOException {
    runBenchmarks(
        Instant.MIN,
        "Start of file");
    runBenchmarks(
        Instant.now().minus(Duration.ofMinutes(5)),
        "5 minutes before end of file");
  }

  private static void runBenchmarks(Instant seekStart, String benchmarkName)
      throws IOException {
    runBenchmark(
        new JumpSearchLogSeeker(
            file,
            EqLogEvent.LINE_PATTERN,
            EqLogEvent.TIMESTAMP_FORMAT,
            ZoneId.systemDefault()),
        seekStart,
        benchmarkName);
    runBenchmark(
        new LinearSearchLogSeeker(
            file,
            EqLogEvent.LINE_PATTERN,
            EqLogEvent.TIMESTAMP_FORMAT,
            ZoneId.systemDefault()),
        seekStart,
        benchmarkName);
  }

  private static void runBenchmark(
      LogSeeker logSeeker,
      Instant seekStart,
      String benchmarkName)
          throws IOException {
    Instant start = Instant.now();
    logSeeker.seek(seekStart);
    Instant end = Instant.now();
    System.out.println(String.format("%s - [%s] benchmark for [%s] strategy",
        getHumanReadableFormat(Duration.between(start, end)),
        benchmarkName,
        logSeeker.getStrategyDescription()));
  }

  // https://stackoverflow.com/a/40487511/192236
  private static String getHumanReadableFormat(Duration duration) {
    return duration.toString()
        .substring(2)
        .replaceAll("(\\d[HMS])(?!$)", "$1 ")
        .toLowerCase();
  }

}