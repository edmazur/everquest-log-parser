package com.edmazur.eqlp.file;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.edmazur.eqlp.EqLogEvent;

class EqLogEventTest {

  @Test
  void goodLine() {
    Optional<EqLogEvent> maybeEqLogEvent = EqLogEvent.parseFromLine(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'hi'");
    assertTrue(maybeEqLogEvent.isPresent());
    EqLogEvent eqLogEvent = maybeEqLogEvent.get();
    assertEquals(
        "[Fri Oct 25 18:58:06 2019] Stanvern says out of character, 'hi'",
        eqLogEvent.getFullLine());
    // TODO: Check timestamp.
    assertEquals(
        "Stanvern says out of character, 'hi'",
        eqLogEvent.getPayload());
  }

  @Test
  void badLine() {
    Optional<EqLogEvent> maybeEqLogEvent = EqLogEvent.parseFromLine(
        "[Wed Sep 29 03:2[Wed Sep 29 08:06:56 2021] Daox begins to cast a spell.");
    assertFalse(maybeEqLogEvent.isPresent());
  }

}
