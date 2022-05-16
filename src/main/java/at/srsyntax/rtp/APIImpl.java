package at.srsyntax.rtp;

import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.api.countdown.CountdownCallback;
import at.srsyntax.rtp.api.countdown.CountdownHandler;
import at.srsyntax.rtp.api.location.LocationCache;
import at.srsyntax.rtp.api.location.TeleportLocation;
import at.srsyntax.rtp.countdown.CountdownHandlerImpl;
import at.srsyntax.rtp.util.TeleportLocationCache;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
public class APIImpl implements API {

  private final RTPPlugin plugin;

  @Override
  public TeleportLocation getLocation(@NotNull String name) {
    for (TeleportLocation location : plugin.getConfig().getLocations()) {
      if (location.getName().equalsIgnoreCase(name))
        return location;
    }
    return null;
  }

  @Override
  public TeleportLocation createLocation(@NotNull String name, @NotNull Location location, @Nullable String permission, int countdown, int cooldown) {
    return createLocation(name, location, permission, countdown, cooldown, null);
  }

  @Override
  public TeleportLocation createLocation(@NotNull String name, @NotNull Location location, @Nullable String permission, int countdown, int cooldown, @Nullable String[] aliases) {
    final TeleportLocationCache teleportLocation = new TeleportLocationCache(
        name, new LocationCache(location),
        permission, countdown, cooldown,
        aliases
    );
    plugin.getConfig().getLocations().add(teleportLocation);
    return teleportLocation;
  }

  @Override
  public void deleteLocation(@NotNull String name) {
    deleteLocation(Objects.requireNonNull(getLocation(name)));
  }

  @Override
  public void deleteLocation(@NotNull TeleportLocation location) {
    plugin.getConfig().getLocations().remove(location);
  }

  @Override
  public CountdownHandler newCountdownHandler(TeleportLocation teleportLocation, Player player, CountdownCallback callback) {
    return new CountdownHandlerImpl(plugin, teleportLocation, player, callback);
  }
}
