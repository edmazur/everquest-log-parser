package com.edmazur.eqlp.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.edmazur.eqlp.EqLogEvent;

class LogSeekerTest {

  private static final String BASIC_LOG = "basic-log.txt";
  private static final String JUNKY_LOG = "junky-log.txt";

  private static final ZoneId TIMEZONE = ZoneId.of("America/New_York");

  private LogSeeker getLogSeeker(String fileName) {
    // TODO: Read this as a local resource.
    File logFile = new File(
        "/home/mazur/git/everquest-log-parser/src/test/resources/" + fileName);
    return new LogSeeker(
        logFile,
        EqLogEvent.LINE_PATTERN,
        EqLogEvent.TIMESTAMP_FORMAT,
        TIMEZONE);
  }

  @Test
  void readBasicWayBeforeFirstLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'line 1'",
        logSeeker.seek(Instant.MIN).readLine());
  }

  @Test
  void readBasicRightBeforeFirstLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'line 1'",
        logSeeker.seek(getInstant("Fri Oct 25 18:58:05 2019")).readLine());
  }

  @Test
  void readBasicRightAtFirstLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'line 1'",
        logSeeker.seek(getInstant("Fri Oct 25 18:58:06 2019")).readLine());
  }

  @Test
  void readBasicRightAfterFirstLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertEquals(
        "[Tue Oct 29 11:46:33 2019] Stanvern says out of character, 'line 2'",
        logSeeker.seek(getInstant("Fri Oct 25 18:58:07 2019")).readLine());
  }

  @Test
  void readBasicRightBeforeNonBoundaryLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertEquals(
        "[Wed Sep 09 00:36:18 2020] Stanvern says out of character, 'line 9'",
        logSeeker.seek(getInstant("Wed Sep 09 00:36:17 2020")).readLine());
  }

  @Test
  void readBasicRightAtNonBoundaryLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertEquals(
        "[Wed Sep 09 00:36:18 2020] Stanvern says out of character, 'line 9'",
        logSeeker.seek(getInstant("Wed Sep 09 00:36:18 2020")).readLine());
  }

  @Test
  void readBasicRightAfterNonBoundaryLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertEquals(
        "[Sun Feb 14 02:46:53 2021] Stanvern says out of character, 'line 10'",
        logSeeker.seek(getInstant("Wed Sep 09 00:36:19 2020")).readLine());
  }

  @Test
  void readBasicRightBeforeLastLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertEquals(
        "[Sun Jul 04 14:34:03 2021] Stanvern says out of character, 'line 13'",
        logSeeker.seek(getInstant("Sun Jul 04 14:34:02 2021")).readLine());
  }

  @Test
  void readBasicRightAtLastLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertEquals(
        "[Sun Jul 04 14:34:03 2021] Stanvern says out of character, 'line 13'",
        logSeeker.seek(getInstant("Sun Jul 04 14:34:03 2021")).readLine());
  }

  @Test
  void readBasicRightAfterLastLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertNull(
        logSeeker.seek(getInstant("Sun Jul 04 14:34:04 2021")).readLine());
  }

  @Test
  void readBasicWayAfterLastLine() throws IOException {
    LogSeeker logSeeker = getLogSeeker(BASIC_LOG);
    assertNull(logSeeker.seek(Instant.MAX).readLine());
  }

  @Test
  void readJunkyRightBeforeLinePrecedingBlank() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:10 2019] Stanvern says out of character, 'oh no, the next line is blank!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:09 2019")).readLine());
  }

  @Test
  void readJunkyRightAtLinePrecedingBlank() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:10 2019] Stanvern says out of character, 'oh no, the next line is blank!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:10 2019")).readLine());
  }

  @Test
  void readJunkyRightAfterLinePrecedingBlank() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:20 2019] Stanvern says out of character, 'whew, got through the blank line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:11 2019")).readLine());
  }

  @Test
  void readJunkyRightBeforeLineProcedingBlank() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:20 2019] Stanvern says out of character, 'whew, got through the blank line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:19 2019")).readLine());
  }

  @Test
  void readJunkyRightAtLineProcedingBlank() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:20 2019] Stanvern says out of character, 'whew, got through the blank line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:20 2019")).readLine());
  }

  @Test
  void readJunkyRightAfterLineProcedingBlank() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:30 2019] Stanvern says out of character, 'wtf happened to the next line?!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:21 2019")).readLine());
  }

  @Test
  void readJunkyRightBeforeLinePrecedingMalformed() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:30 2019] Stanvern says out of character, 'wtf happened to the next line?!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:29 2019")).readLine());
  }

  @Test
  void readJunkyRightAtLinePrecedingMalformed() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:30 2019] Stanvern says out of character, 'wtf happened to the next line?!'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:30 2019")).readLine());
  }

  @Test
  void readJunkyRightAfterLinePrecedingMalformed() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:50 2019] Stanvern says out of character, 'whew, got through the malformed line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:31 2019")).readLine());
  }

  @Test
  void readJunkyRightBeforeLineProcedingMalformed() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:50 2019] Stanvern says out of character, 'whew, got through the malformed line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:49 2019")).readLine());
  }

  @Test
  void readJunkyRightAtLineProcedingMalformed() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
    assertEquals(
        "[Fri Oct 25 00:00:50 2019] Stanvern says out of character, 'whew, got through the malformed line'",
        logSeeker.seek(getInstant("Fri Oct 25 00:00:50 2019")).readLine());
  }

  @Test
  void readJunkyRightAfterLineProcedingMalformed() throws IOException {
    LogSeeker logSeeker = getLogSeeker(JUNKY_LOG);
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