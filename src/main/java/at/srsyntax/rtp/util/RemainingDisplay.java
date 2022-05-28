package at.srsyntax.rtp.util;

import org.jetbrains.annotations.NotNull;

/*
 * MIT License
 *
 * Copyright (c) 2022 Marcel Haberl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class RemainingDisplay {

  public static int[] getRemainingTimeArray(long milli) {
    if (milli < 1000) throw new IllegalArgumentException();

    final long currentTime = System.currentTimeMillis();
    long time = currentTime > milli ? currentTime - milli : milli - currentTime;
    int days = 0,
        hours = 0,
        minutes = 0,
        seconds;

    if ((time /= 1000) >= 86400)
      days = (int)time / 86400;

    if ((time -= days * 86400) > 3600)
      hours = (int)time / 3600;

    if ((time -= hours * 3600) > 60)
      minutes = (int)time / 60;

    seconds = (int) (time - (minutes * 60));

    return new int[]{days, hours, minutes, seconds};
  }

  public static @NotNull String getRemainingTime(long time) {
    final int[] end = getRemainingTimeArray(time);
    final StringBuilder stringBuilder = new StringBuilder();
    final int days = end[0],
        hours = end[1],
        minutes = end[2],
        seconds = end[3];

    boolean showSeconds = true;

    if (days != 0) {
      stringBuilder.append(days == 1 ? days + " <day> " : days + " <days> ");
      showSeconds = false;
    }

    if (hours != 0) {
      stringBuilder.append(hours == 1 ? hours + " <hour> " : hours + " <hours> ");
      showSeconds = false;
    }

    if (minutes != 0) {
      stringBuilder.append(minutes == 1 ? minutes + " <minute> " : minutes + " <minutes> ");
      showSeconds = false;
    }

    if (seconds != 0 && showSeconds)
      stringBuilder.append(seconds == 1 ? seconds + " <second> " : seconds + " <seconds> ");

    return stringBuilder.toString();
  }
}
