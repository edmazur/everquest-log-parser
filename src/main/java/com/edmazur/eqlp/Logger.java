package com.edmazur.eqlp;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

  private static final boolean DEBUG = false;

  private static final Format DATE_FORMAT = new SimpleDateFormat("MMdd HH:mm:ss.SSS");

  public void log(String message) {
    if (DEBUG) {
      System.out.println(String.format("%s [%s] [%s:%d] %s",
          DATE_FORMAT.format(new Date()),
          Thread.currentThread().getName(),
          Thread.currentThread().getStackTrace()[2].getClassName(),
          Thread.currentThread().getStackTrace()[2].getLineNumber(),
          message));
    }
  }
}
