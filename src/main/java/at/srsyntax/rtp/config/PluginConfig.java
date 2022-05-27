package at.srsyntax.rtp.config;

import at.srsyntax.rtp.api.location.LocationCache;
import at.srsyntax.rtp.util.TeleportLocationCache;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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
public class PluginConfig extends ConfigLoader {

  private final boolean prefix;
  private final boolean offline;
  private final int cacheSize;
  private final double refundPercent;
  private final List<TeleportLocationCache> locations;
  private final String defaultLocation;

  private final MessageConfig message;

  public PluginConfig() {
    this.prefix = true;
    this.offline = false;

    this.cacheSize = 3;
    this.refundPercent = 1D;
    this.locations = new ArrayList<>();
    final TeleportLocationCache locationTemplate = new TeleportLocationCache(
        "Template", new LocationCache(),
        "syntaxrtp.teleport.Template",
        10000, 5, 60, 100,
        new String[]{}
    );
    locations.add(locationTemplate);
    this.defaultLocation = locationTemplate.getName();

    this.message = new MessageConfig();
  }
}
