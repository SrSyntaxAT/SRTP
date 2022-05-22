package at.srsyntax.rtp.config;

import lombok.Getter;

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
@Getter
public class MessageConfig {

  private final String prefix;

  private final String cooldown;
  private final String countdown, countdownCanceled, countdownAlreadyActive;

  private final String teleported;
  private final String noPermission, locationNotFound;

  public MessageConfig() {
    this.prefix = "&8[&6SyntaxRTP&8]";

    this.cooldown = "&cYou may not use this command again for &e<remaining>&c.";

    this.countdown = "&cYou will be teleported in &e<time> seconds.";
    this.countdownCanceled = "&cTeleportation was canceled because you moved.";
    this.countdownAlreadyActive = "&cThere is already a countdown active.";

    this.teleported = "&aYou were teleported.";

    this.noPermission = "&cYou have no rights to do that!";
    this.locationNotFound = "&cUnknown rtp location!";
  }
}
