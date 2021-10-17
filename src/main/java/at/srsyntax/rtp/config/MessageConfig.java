package at.srsyntax.rtp.config;

/*
 * MIT License
 *
 * Copyright (c) 2021 Marcel Haberl
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
public class MessageConfig {

  private final String[] teleportCountdown;
  private final String prefix, teleportedWithoutCountdown, teleportOther, teleportCooldownError, noPermission, teleportError, playerNotFound;
  private final String seconds, second;

  public MessageConfig(String[] teleportCountdown, String prefix, String teleportedWithoutCountdown, String teleportOther, String teleportCooldownError, String noPermission, String teleportError, String playerNotFound, String second, String seconds) {
    this.teleportCountdown = teleportCountdown;
    this.prefix = prefix;
    this.teleportedWithoutCountdown = teleportedWithoutCountdown;
    this.teleportOther = teleportOther;
    this.teleportCooldownError = teleportCooldownError;
    this.noPermission = noPermission;
    this.teleportError = teleportError;
    this.playerNotFound = playerNotFound;
    this.second = second;
    this.seconds = seconds;
  }

  public String[] getTeleportCountdown() {
    return teleportCountdown;
  }

  public String getTeleportOther() {
    return teleportOther;
  }

  public String getTeleportCooldownError() {
    return teleportCooldownError;
  }

  public String getNoPermission() {
    return noPermission;
  }

  public String getTeleportError() {
    return teleportError;
  }

  public String getPlayerNotFound() {
    return playerNotFound;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getSeconds() {
    return seconds;
  }

  public String getSecond() {
    return second;
  }

  public String getTeleportedWithoutCountdown() {
    return teleportedWithoutCountdown;
  }
}
