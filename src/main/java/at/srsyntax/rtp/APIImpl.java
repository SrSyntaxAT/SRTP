package at.srsyntax.rtp;

import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.api.event.PlayerRandomTeleportEvent;
import at.srsyntax.rtp.api.handler.cooldown.CooldownHandler;
import at.srsyntax.rtp.api.handler.countdown.CountdownCallback;
import at.srsyntax.rtp.api.handler.countdown.CountdownHandler;
import at.srsyntax.rtp.api.handler.economy.EconomyHandler;
import at.srsyntax.rtp.api.location.LocationCache;
import at.srsyntax.rtp.api.location.TeleportLocation;
import at.srsyntax.rtp.database.repository.location.LocationRepository;
import at.srsyntax.rtp.handler.cooldown.CooldownHandlerImpl;
import at.srsyntax.rtp.handler.countdown.CountdownHandlerImpl;
import at.srsyntax.rtp.handler.economy.EconomyHandlerImpl;
import at.srsyntax.rtp.api.location.LocationRandomizer;
import at.srsyntax.rtp.util.TeleportLocationCache;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
public class APIImpl implements API {

  private final RTPPlugin plugin;
  private final Economy economy;

  public APIImpl(RTPPlugin plugin) {
    this.plugin = plugin;
    this.economy = setupEconomy();
  }

  private Economy setupEconomy() {
    if (Bukkit.getPluginManager().getPlugin("Vault") == null) return null;

    final RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
    if (rsp == null) return null;

    return rsp.getProvider();
  }

  @Override
  public TeleportLocation getLocation(@NotNull String name) {
    for (TeleportLocation location : plugin.getPluginConfig().getLocations()) {
      if (location.getName().equalsIgnoreCase(name))
        return location;
    }
    return null;
  }

  @Override
  public @NotNull TeleportLocation createLocation(@NotNull String name, @NotNull Location location, @Nullable String permission, int size, int countdown, int cooldown, double price) {
    return createLocation(name, location, permission, size, countdown, cooldown, price, null);
  }

  @Override
  public @NotNull TeleportLocation createLocation(@NotNull String name, @NotNull Location location, @Nullable String permission, int size, int countdown, int cooldown, double price, @Nullable String[] aliases) {
    final TeleportLocationCache teleportLocation = new TeleportLocationCache(
        name, new LocationCache(location),
        permission, size, countdown, cooldown,
        price, aliases
    );
    plugin.getPluginConfig().getLocations().add(teleportLocation);
    plugin.generateLocationLoader(teleportLocation).load();
    return teleportLocation;
  }

  @Override
  public void deleteLocation(@NotNull String name) {
    deleteLocation(Objects.requireNonNull(getLocation(name)));
  }

  @Override
  public void deleteLocation(@NotNull TeleportLocation location) {
    try {
      plugin.getPluginConfig().getLocations().remove(location);
      plugin.getDatabase().getLocationRepository().removeLocations(location);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NotNull List<TeleportLocation> getLocationsCopy() {
    return new ArrayList<>(plugin.getPluginConfig().getLocations());
  }

  @Override
  public void teleport(@NotNull Player player, @NotNull TeleportLocation location) {
    final TeleportLocationCache teleportLocationCache = (TeleportLocationCache) location;
    final LocationCache cache = teleportLocationCache.getLocationCaches().removeFirst();
    player.teleport(cache.toBukkit());
    Bukkit.getPluginManager().callEvent(new PlayerRandomTeleportEvent(player, teleportLocationCache));
    Bukkit.getScheduler().runTaskAsynchronously(plugin, generateLocationRunnable(teleportLocationCache, cache));
  }

  @Override
  public void teleport(@NotNull Player player, @NotNull Location center, int size) {
    teleport(player, Objects.requireNonNull(center.getWorld()), center.getBlockX(), center.getBlockZ(), size);
  }

  @Override
  public void teleport(@NotNull Player player, @NotNull World world, int centerX, int centerZ, int size) {
    player.teleport(new LocationRandomizer(world, size).meanCoordinate(centerX, centerZ).random());
  }

  private Runnable generateLocationRunnable(TeleportLocationCache teleportLocation, LocationCache cache) {
    return () -> {
      try {
        final LocationRepository repository = plugin.getDatabase().getLocationRepository();

        teleportLocation.getLocationCaches().remove(cache);
        repository.removeLocation(cache);

        final LocationRandomizer randomizer = new LocationRandomizer(cache.toBukkit().getWorld(), teleportLocation.getSize());
        final LocationCache newLocation = new LocationCache(randomizer.random());

        repository.addLocation(teleportLocation, newLocation);
        teleportLocation.getLocationCaches().add(newLocation);

        Bukkit.getScheduler().runTask(plugin, newLocation::loadChunk);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @Override
  public @NotNull CountdownHandler newCountdownHandler(@NotNull TeleportLocation teleportLocation, @NotNull Player player, @NotNull CountdownCallback callback) {
    return new CountdownHandlerImpl(plugin, teleportLocation, player, callback);
  }

  @Override
  public @NotNull CooldownHandler newCooldownHandler(@NotNull TeleportLocation teleportLocation, @NotNull Player player) {
    return new CooldownHandlerImpl(plugin, teleportLocation, player);
  }

  @Override
  public @NotNull EconomyHandler newEconomyHandler(@NotNull TeleportLocation teleportLocation, @NotNull Player player) {
    return new EconomyHandlerImpl(economy, teleportLocation, player);
  }

  @Override
  public boolean hasActivCountdown(@NotNull Player player) {
    return plugin.getCountdownHandlerMap().containsKey(player);
  }

  @Override
  public CountdownHandler getCountdownHandler(@NotNull Player player) {
    return plugin.getCountdownHandlerMap().get(player);
  }

  @Override
  public boolean isVaultSupported() {
    return economy != null;
  }
}
