package at.srsyntax.rtp;

import at.srsyntax.rtp.config.PluginConfig;
import at.srsyntax.rtp.util.VersionCheck;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

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
public class RTPPlugin extends JavaPlugin {

  public static final int RESOURCE_ID = 99428, BSTATS_ID = 13408;

  @Override
  public void onEnable() {
    try {
      checkVersion();
      new Metrics(this, BSTATS_ID);

      final PluginConfig config = (PluginConfig) PluginConfig.load(this, new PluginConfig());
    } catch (Exception exception) {
      getLogger().severe("Plugin could not be loaded successfully!");
      exception.printStackTrace();
    }
  }

  private void checkVersion() {
    try {
      final VersionCheck check = new VersionCheck(getDescription().getVersion(), RESOURCE_ID);
      if (check.check()) return;
      getLogger().warning("The plugin is no longer up to date, please update the plugin.");
    } catch (Exception ignored) {}
  }
}
