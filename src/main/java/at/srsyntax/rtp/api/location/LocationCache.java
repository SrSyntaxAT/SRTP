package at.srsyntax.rtp.api.location;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

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
public class LocationCache {

  @Getter private transient final String id;

  private final String world;
  private final double x, y, z;
  private final float yaw, pitch;

  public LocationCache(Location location) {
    this(
        UUID.randomUUID().toString(),
        location.getWorld().getName(),
        location.getX(),
        location.getY(),
        location.getZ(),
        location.getYaw(),
        location.getPitch()
    );
  }

  public LocationCache(String id, LocationCache cache) {
    this(id, cache.world, cache.x, cache.y, cache.z, cache.yaw, cache.pitch);
  }

  public static LocationCache fromJson(String id, String json) {
    return new LocationCache(id, new Gson().fromJson(json, LocationCache.class));
  }

  public void loadChunk() {
    toBukkit().getChunk().load(true);
  }

  @Override
  public String toString() {
    return new GsonBuilder().disableHtmlEscaping().create().toJson(this);
  }

  public Location toBukkit() {
    return new Location(
        Bukkit.getWorld(world),
        x, y, z,
        yaw, pitch
    );
  }
}
