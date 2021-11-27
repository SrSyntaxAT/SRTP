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
public class PermissionConfig {

  private final String teleport, teleportOther, teleportCountdownBypass, teleportCooldownBypass, reload;

  public PermissionConfig(String teleport, String teleportOther, String teleportCountdownBypass, String teleportCooldownBypass, String reload) {
    this.teleport = teleport;
    this.teleportOther = teleportOther;
    this.teleportCountdownBypass = teleportCountdownBypass;
    this.teleportCooldownBypass = teleportCooldownBypass;
    this.reload = reload;
  }

  public String getTeleport() {
    return teleport;
  }

  public String getTeleportOther() {
    return teleportOther;
  }

  public String getTeleportCountdownBypass() {
    return teleportCountdownBypass;
  }

  public String getTeleportCooldownBypass() {
    return teleportCooldownBypass;
  }
  
  public String getReload() {
    return reload;
  }
}
