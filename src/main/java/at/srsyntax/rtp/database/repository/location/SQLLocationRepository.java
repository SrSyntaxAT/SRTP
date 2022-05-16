package at.srsyntax.rtp.database.repository.location;

import at.srsyntax.rtp.api.location.LocationCache;
import at.srsyntax.rtp.api.location.TeleportLocation;
import at.srsyntax.rtp.database.Database;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
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
@AllArgsConstructor
public class SQLLocationRepository implements LocationRepository {

  private final Database database;

  @Override
  public void createTable() throws SQLException {
    final String sql = "CREATE TABLE IF NOT EXISTS location_cache (rtp TEXT, id TEXT, location TEXT)";
    connection().createStatement().execute(sql);
  }

  @Override
  public Connection connection() {
    return database.getConnection();
  }

  @Override
  public List<LocationCache> getLocations(TeleportLocation teleportLocation) throws SQLException {
    final String sql = "SEKECT id, location FROM location_cache WHERE rtp = ?";
    final PreparedStatement statement = connection().prepareStatement(sql);
    statement.setString(1, teleportLocation.getName());
    return readResultSet(statement.executeQuery());
  }

  private List<LocationCache> readResultSet(ResultSet resultSet) throws SQLException {
    if (resultSet == null) return null;
    final List<LocationCache> caches = new LinkedList<>();

    while (resultSet.next()) {
      caches.add(LocationCache.fromJson(resultSet.getString("id"), resultSet.getString("location")));
    }

    return caches;
  }

  @Override
  public void removeLocation(LocationCache cache) throws SQLException {
    final String sql = "DELETE FROM  location_cache WHERE id = ?";
    final PreparedStatement statement = connection().prepareStatement(sql);
    statement.setString(1, cache.getId());
    statement.execute();
  }

  @Override
  public void removeLocations(TeleportLocation teleportLocation) throws SQLException {
    final String sql = "DELETE FROM location_cache WHERE rtp = ?";
    final PreparedStatement statement = connection().prepareStatement(sql);
    statement.setString(1, teleportLocation.getName());
    statement.execute();
  }

  @Override
  public void addLocation(TeleportLocation teleportLocation, LocationCache cache) throws SQLException {
    final String sql = "INSERT INTO location_cache (rtp, id, location) VALUES (?,?,?)";
    final PreparedStatement statement = connection().prepareStatement(sql);
    statement.setString(1, teleportLocation.getName());
    statement.setString(2, cache.getId());
    statement.setString(3, cache.toString());
    statement.execute();
  }
}
