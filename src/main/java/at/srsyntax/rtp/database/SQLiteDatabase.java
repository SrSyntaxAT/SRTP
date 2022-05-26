package at.srsyntax.rtp.database;

import at.srsyntax.rtp.RTPPlugin;
import at.srsyntax.rtp.database.repository.cooldown.CooldownRepository;
import at.srsyntax.rtp.database.repository.cooldown.SQLCooldownRepository;
import at.srsyntax.rtp.database.repository.location.LocationRepository;
import at.srsyntax.rtp.database.repository.location.SQLLocationRepository;
import lombok.Getter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
public class SQLiteDatabase implements Database {

  private final RTPPlugin plugin;
  private Connection connection;

  @Getter private final LocationRepository locationRepository;
  @Getter private final CooldownRepository cooldownRepository;

  public SQLiteDatabase(RTPPlugin plugin) {
    this.plugin = plugin;
    this.locationRepository = new SQLLocationRepository(this);
    this.cooldownRepository = new SQLCooldownRepository(plugin, this, plugin.getPluginConfig().isOffline());
  }

  @Override
  public void connect() throws SQLException {
    final File file = new File(this.plugin.getDataFolder(), "database.db");
    connection = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());

    locationRepository.createTable();
    cooldownRepository.createTable();
  }

  @Override
  public void disconnect() {
    try {
      if (connection != null && !connection.isClosed())
        connection.close();
    } catch (SQLException ignored) {}
  }

  @Override
  public boolean isConnected() throws SQLException {
    return connection != null && !connection.isClosed();
  }

  @Override
  public Connection getConnection() {
    try {
      if (connection == null || connection.isClosed())
        connect();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return connection;
  }

}
