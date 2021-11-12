package com.edmazur.eqlp.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.edmazur.eqlp.EqLogEvent;

class LogSeekerTest {

  private static final ZoneId TIMEZONE = ZoneId.of("America/New_York");

  private LogSeeker logSeeker = null;

  @BeforeEach
  void setUp() throws Exception {
    // TODO: Read this as a local resource.
    File logFile = new File(
        "/home/mazur/git/everquest-log-parser/src/test/resources/basic-log.txt");
    logSeeker = new LogSeeker(
        logFile,
        EqLogEvent.LINE_PATTERN,
        EqLogEvent.TIMESTAMP_FORMAT,
        TIMEZONE);
  }

  @Test
  void readWayBeforeFirstLine() throws IOException {
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'line 1'",
        logSeeker.seek(Instant.MIN).readLine());
  }

  @Test
  void readRightBeforeFirstLine() throws IOException {
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'line 1'",
        logSeeker.seek(getInstant("Fri Oct 25 18:58:05 2019")).readLine());
  }

  @Test
  void readRightAtFirstLine() throws IOException {
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'line 1'",
        logSeeker.seek(getInstant("Fri Oct 25 18:58:06 2019")).readLine());
  }

  @Test
  void readRightAfterFirstLine() throws IOException {
    assertEquals(
        "[Tue Oct 29 11:46:33 2019] Stanvern says out of character, 'line 2'",
        logSeeker.seek(getInstant("Fri Oct 25 18:58:07 2019")).readLine());
  }

  @Test
  void readRightBeforeNonBoundaryLine() throws IOException {
    assertEquals(
        "[Wed Sep 09 00:36:18 2020] Stanvern says out of character, 'line 9'",
        logSeeker.seek(getInstant("Wed Sep 09 00:36:17 2020")).readLine());
  }

  @Test
  void readRightAtNonBoundaryLine() throws IOException {
    assertEquals(
        "[Wed Sep 09 00:36:18 2020] Stanvern says out of character, 'line 9'",
        logSeeker.seek(getInstant("Wed Sep 09 00:36:18 2020")).readLine());
  }

  @Test
  void readRightAfterNonBoundaryLine() throws IOException {
    assertEquals(
        "[Sun Feb 14 02:46:53 2021] Stanvern says out of character, 'line 10'",
        logSeeker.seek(getInstant("Wed Sep 09 00:36:19 2020")).readLine());
  }

  @Test
  void readRightBeforeLastLine() throws IOException {
    assertEquals(
        "[Sun Jul 04 14:34:03 2021] Stanvern says out of character, 'line 13'",
        logSeeker.seek(getInstant("Sun Jul 04 14:34:02 2021")).readLine());
  }

  @Test
  void readRightAtLastLine() throws IOException {
    assertEquals(
        "[Sun Jul 04 14:34:03 2021] Stanvern says out of character, 'line 13'",
        logSeeker.seek(getInstant("Sun Jul 04 14:34:03 2021")).readLine());
  }

  @Test
  void readRightAfterLastLine() throws IOException {
    assertNull(
        logSeeker.seek(getInstant("Sun Jul 04 14:34:04 2021")).readLine());
  }

  @Test
  void readWayAfterLastLine() throws IOException {
    assertNull(logSeeker.seek(Instant.MAX).readLine());
  }

  private Instant getInstant(String timestamp) {
    return LocalDateTime
        .parse(timestamp, EqLogEvent.TIMESTAMP_FORMAT)
        .atZone(TIMEZONE)
        .toInstant();
  }

}