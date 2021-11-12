package com.edmazur.eqlp;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;

public class BasicEqLogReader {

  public static void main(String[] args) {
    final Path eqInstallDirectory = Paths.get(args[0]);
    final ZoneId timezone = ZoneId.of(args[1]);
    final String server = args[2];
    final String character = args[3];

    EqLog eqLog = new EqLog(
        eqInstallDirectory,
        timezone,
        server,
        character,
        Instant.MIN,
        Instant.MAX);
    eqLog.addListener(new EqLogListener() {
      @Override
      public void onEvent(EqLogEvent eqLogEvent) {
        System.out.println(eqLogEvent);
      }
    });
    eqLog.run();
  }

}