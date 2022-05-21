package at.srsyntax.rtp;

import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.config.PluginConfig;
import at.srsyntax.rtp.database.Database;
import at.srsyntax.rtp.database.SQLiteDatabase;
import at.srsyntax.rtp.util.VersionCheck;
import lombok.Getter;
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
  public static final String ADMIN_PERMISSION = "syntaxrtp.admin";

  private static API api;

  @Getter private PluginConfig config;
  @Getter private Database database;

  @Override
  public void onEnable() {
    try {
      checkVersion();
      new Metrics(this, BSTATS_ID);

      api = new APIImpl(this);

      config = (PluginConfig) PluginConfig.load(this, new PluginConfig());
      database = new SQLiteDatabase(this);
    } catch (Exception exception) {
      getLogger().severe("Plugin could not be loaded successfully!");
      exception.printStackTrace();
    }
  }

  @Override
  public void onDisable() {
    database.disconnect();
  }

  private void checkVersion() {
    try {
      if (new VersionCheck(getDescription().getVersion(), RESOURCE_ID).check()) return;
      getLogger().warning("The plugin is no longer up to date, please update the plugin.");
    } catch (Exception ignored) {}
  }

  public static API getApi() {
    return api;
  }
}
