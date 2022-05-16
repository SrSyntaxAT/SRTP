package at.srsyntax.rtp.database.repository.cooldown;

import at.srsyntax.rtp.RTPPlugin;
import at.srsyntax.rtp.api.location.TeleportLocation;
import at.srsyntax.rtp.cooldown.CooldownHandlerImpl;
import at.srsyntax.rtp.database.Database;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
@AllArgsConstructor
public class SQLCooldownRepository implements CooldownRepository {

  private final Plugin plugin;
  private final Database database;
  private final  boolean offline;

  @Override
  public void createTable() throws SQLException {
    connection().createStatement().execute("CREATE TABLE IF NOT EXISTS cooldown (player TEXT, rtp TEXT, end INTEGER)");
  }

  @Override
  public Connection connection() {
    return database.getConnection();
  }

  @Override
  public void delete(Player player, TeleportLocation teleportLocation) throws SQLException {
    delete(player, teleportLocation.getName());
  }

  private void delete(Player player, String rtp) throws SQLException {
    final String sql = "DELETE FROM cooldown WHERE player=? AND rtp=?";
    final PreparedStatement statement = connection().prepareStatement(sql);
    statement.setString(1, playerDatabaseName(player));
    statement.setString(2, rtp);
    statement.execute();
  }

  @Override
  public void add(Player player, TeleportLocation teleportLocation, long end) throws SQLException {
    final String sql = "INSERT INTO cooldown (player,rtp,end) VALUES (?,?,?)";
    final PreparedStatement statement = connection().prepareStatement(sql);
    statement.setString(1, playerDatabaseName(player));
    statement.setString(2, teleportLocation.getName());
    statement.setLong(3, end);
    statement.execute();
  }

  @Override
  public void insertData(Player player) throws SQLException {
    final ResultSet resultSet = getPlayerData(player);

    while (resultSet != null && resultSet.next()) {
      final String rtp = resultSet.getString("rtp");
      if (RTPPlugin.getApi().getLocation(rtp) == null) {
        delete(player, rtp);
        continue;
      }

      final String key = CooldownHandlerImpl.METADATA_KEY + rtp;
      player.setMetadata(key, new FixedMetadataValue(plugin, resultSet.getLong("end")));
    }
  }

  private ResultSet getPlayerData(Player player) throws SQLException {
    final String sql = "SELECT rtp,end FROM cooldown WHERE player = ?";
    final PreparedStatement statement = connection().prepareStatement(sql);
    statement.setString(1, playerDatabaseName(player));
    return statement.executeQuery();
  }

  private String playerDatabaseName(Player player) {
    return offline ? player.getName() : player.getUniqueId().toString();
  }
}
