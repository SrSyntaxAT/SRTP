package at.srsyntax.rtp.util;

import at.srsyntax.rtp.api.location.LocationCache;
import at.srsyntax.rtp.database.repository.location.LocationRepository;
import lombok.AllArgsConstructor;
import org.bukkit.Location;

import java.sql.SQLException;
import java.util.logging.Logger;

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
public class LocationLoader {

  private final Logger logger;
  private final LocationRepository repository;
  private final TeleportLocationCache location;
  private final int cacheSize;

  public void load() {
    final String name = location.getName();

    try {
      logger.info("Load " + name);
      loadLocationCache();
      checkCacheSize();
      loadCache();
      logger.info( name + " was loaded successfully.");
    } catch (Exception exception) {
      logger.severe(name + " could not be loaded successfully.");
      exception.printStackTrace();
    }
  }

  private void loadLocationCache() throws SQLException {
    location.getLocationCaches().addAll(repository.getLocations(location));
  }

  private void checkCacheSize() throws SQLException {
    if (location.getLocationCaches().size() >= cacheSize) return;
    final LocationRandomizer randomizer = new LocationRandomizer(location.getLocation().toBukkit().getWorld(), location.getSize());

    while (location.getLocationCaches().size() < cacheSize) {
      final LocationCache cache = new LocationCache(randomizer.random());
      location.getLocationCaches().add(cache);
      repository.addLocation(location, cache);
    }
  }

  private void loadCache() {
    location.getLocationCaches().forEach(LocationCache::loadChunk);
  }
}
