package com.edmazur.eqlp.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.edmazur.eqlp.EqLogEvent;

// Tests all implementations (and variations of each as needed) of LogSeeker
// using a common set of test cases.
class LogSeekerTest {

  // TODO: Replace with Supplier<LogSeeker> once Guava is added to project.
  private interface LogSeekerProvider {
    LogSeeker get(String fileName);
  }

  private static final String BASIC_LOG = "basic-log.txt";
  private static final String JUNKY_LOG = "junky-log.txt";

  private static final ZoneId TIMEZONE = ZoneId.of("America/New_York");

  // There are a couple things going on here:
  // - This provides providers (rather than LogSeeker directly) because
  //   LogSeeker can't be instantiated without knowing the log file to test,
  //   which is decided by the test case.
  // - The string argument provided would ideally be obtained via
  //   getStrategyDescription() dynamically, but there doesn't seem to be a way
  //   to set this inside the actual test case (since the test case gets a
  //   LogSeekerProvider and not a LogSeeker (see point above), that's the
  //   earliest this can be done.
  // TODO: Consider using Named<T> interface (https://stackoverflow.com/a/66994980/192236).
  private static Stream<Arguments> provideLogSeekerProviders() {
    return Stream.of(
        Arguments.of(
            "Linear search",
            new LogSeekerProvider() {
              @Override
              public LogSeeker get(String fileName) {
                return new LinearSearchLogSeeker(
                    getFile(fileName),
                    EqLogEvent.LINE_PATTERN,
                    EqLogEvent.TIMESTAMP_FORMAT,
                    TIMEZONE);
              }
            }),

        Arguments.of(
            "Jump search with jumpSize=1",
            new LogSeekerProvider() {
              @Override
              public LogSeeker get(String fileName) {
                return new JumpSearchLogSeeker(
                    getFile(fileName),
                    EqLogEvent.LINE_PATTERN,
                    EqLogEvent.TIMESTAMP_FORMAT,
                    TIMEZONE,
                    1);
              }
            }),

        Arguments.of(
            "Jump search with jumpSize=2",
            new LogSeekerProvider() {
              @Override
              public LogSeeker get(String fileName) {
                return new JumpSearchLogSeeker(
                    getFile(fileName),
                    EqLogEvent.LINE_PATTERN,
                    EqLogEvent.TIMESTAMP_FORMAT,
                    TIMEZONE,
                    2);
              }
            }),

        Arguments.of(
            "Jump search with jumpSize=10",
            new LogSeekerProvider() {
              @Override
              public LogSeeker get(String fileName) {
                return new JumpSearchLogSeeker(
                    getFile(fileName),
                    EqLogEvent.LINE_PATTERN,
                    EqLogEvent.TIMESTAMP_FORMAT,
                    TIMEZONE,
                    10);
              }
            }),

        Arguments.of(
            "Jump search with default jumpSize",
            new LogSeekerProvider() {
              @Override
              public LogSeeker get(String fileName) {
                return new JumpSearchLogSeeker(
                    getFile(fileName),
                    EqLogEvent.LINE_PATTERN,
                    EqLogEvent.TIMESTAMP_FORMAT,
                    TIMEZONE);
              }
            })

        );
  }

  private static File getFile(String fileName) {
    // TODO: Read this as a local resource.
    return new File(
        "/home/mazur/git/everquest-log-parser/src/test/resources/" + fileName);
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicWayBeforeFirstLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'line 1'",
        logSeeker.seek(Instant.MIN).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicRightBeforeFirstLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'line 1'",
        logSeeker.seek(getInstant("Fri Oct 25 18:58:05 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicRightAtFirstLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'line 1'",
        logSeeker.seek(getInstant("Fri Oct 25 18:58:06 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicRightAfterFirstLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertEquals(
        "[Tue Oct 29 11:46:33 2019] Stanvern says out of character, 'line 2'",
        logSeeker.seek(getInstant("Fri Oct 25 18:58:07 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicRightBeforeNonBoundaryLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertEquals(
        "[Wed Sep 09 00:36:18 2020] Stanvern says out of character, 'line 9'",
        logSeeker.seek(getInstant("Wed Sep 09 00:36:17 2020")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicRightAtNonBoundaryLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertEquals(
        "[Wed Sep 09 00:36:18 2020] Stanvern says out of character, 'line 9'",
        logSeeker.seek(getInstant("Wed Sep 09 00:36:18 2020")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicRightAfterNonBoundaryLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertEquals(
        "[Sun Feb 14 02:46:53 2021] Stanvern says out of character, 'line 10'",
        logSeeker.seek(getInstant("Wed Sep 09 00:36:19 2020")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicRightBeforeLastLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertEquals(
        "[Sun Jul 04 14:34:03 2021] Stanvern says out of character, 'line 13'",
        logSeeker.seek(getInstant("Sun Jul 04 14:34:02 2021")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicRightAtLastLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertEquals(
        "[Sun Jul 04 14:34:03 2021] Stanvern says out of character, 'line 13'",
        logSeeker.seek(getInstant("Sun Jul 04 14:34:03 2021")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicRightAfterLastLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertNull(
        logSeeker.seek(getInstant("Sun Jul 04 14:34:04 2021")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readBasicWayAfterLastLine(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(BASIC_LOG);
    assertNull(logSeeker.seek(Instant.MAX).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightBeforeLinePrecedingBlank(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:10 2019] Stanvern says out of character, 'oh no, the next line is blank!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:09 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightAtLinePrecedingBlank(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:10 2019] Stanvern says out of character, 'oh no, the next line is blank!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:10 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightAfterLinePrecedingBlank(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:20 2019] Stanvern says out of character, 'whew, got through the blank line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:11 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightBeforeLineProcedingBlank(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:20 2019] Stanvern says out of character, 'whew, got through the blank line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:19 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightAtLineProcedingBlank(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:20 2019] Stanvern says out of character, 'whew, got through the blank line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:20 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightAfterLineProcedingBlank(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:30 2019] Stanvern says out of character, 'wtf happened to the next line?!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:21 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightBeforeLinePrecedingMalformed(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:30 2019] Stanvern says out of character, 'wtf happened to the next line?!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:29 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightAtLinePrecedingMalformed(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:30 2019] Stanvern says out of character, 'wtf happened to the next line?!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:30 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightAfterLinePrecedingMalformed(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:50 2019] Stanvern says out of character, 'whew, got through the malformed line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:31 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightBeforeLineProcedingMalformed(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:50 2019] Stanvern says out of character, 'whew, got through the malformed line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:49 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightAtLineProcedingMalformed(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:50 2019] Stanvern says out of character, 'whew, got through the malformed line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:50 2019")).readLine());
  }

  @ParameterizedTest
  @MethodSource("provideLogSeekerProviders")
  void readJunkyRightAfterLineProcedingMalformed(
      String logSeekerName,
      LogSeekerProvider logSeekerProvider) throws IOException {
    LogSeeker logSeeker = logSeekerProvider.get(JUNKY_LOG);
    assertNull(
        logSeeker.seek(getInstant("Fri Oct 25 00:00:51 2019")).readLine());
  }

  private Instant getInstant(String timestamp) {
    return LocalDateTime
        .parse(timestamp, EqLogEvent.TIMESTAMP_FORMAT)
        .atZone(TIMEZONE)
        .toInstant();
  }

}