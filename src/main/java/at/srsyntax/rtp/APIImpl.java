package at.srsyntax.rtp;

import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.api.cooldown.CooldownHandler;
import at.srsyntax.rtp.api.countdown.CountdownCallback;
import at.srsyntax.rtp.api.countdown.CountdownHandler;
import at.srsyntax.rtp.api.handler.economy.EconomyHandler;
import at.srsyntax.rtp.api.location.LocationCache;
import at.srsyntax.rtp.api.location.TeleportLocation;
import at.srsyntax.rtp.cooldown.CooldownHandlerImpl;
import at.srsyntax.rtp.countdown.CountdownHandlerImpl;
import at.srsyntax.rtp.economy.EconomyHandlerImpl;
import at.srsyntax.rtp.util.TeleportLocationCache;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
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
    for (TeleportLocation location : plugin.getConfig().getLocations()) {
      if (location.getName().equalsIgnoreCase(name))
        return location;
    }
    return null;
  }

  @Override
  public TeleportLocation createLocation(@NotNull String name, @NotNull Location location, @Nullable String permission, int countdown, int cooldown, double price) {
    return createLocation(name, location, permission, countdown, cooldown, price, null);
  }

  @Override
  public TeleportLocation createLocation(@NotNull String name, @NotNull Location location, @Nullable String permission, int countdown, int cooldown, double price, @Nullable String[] aliases) {
    final TeleportLocationCache teleportLocation = new TeleportLocationCache(
        name, new LocationCache(location),
        permission, countdown, cooldown,
        price, aliases
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

  @Override
  public CooldownHandler newCooldownHandler(TeleportLocation teleportLocation, Player player) {
    return new CooldownHandlerImpl(plugin, teleportLocation, player);
  }

  @Override
  public EconomyHandler newEconomyHandler(TeleportLocation teleportLocation, Player player) {
    return new EconomyHandlerImpl(economy, teleportLocation, player);
  }

  @Override
  public boolean isVaultSupported() {
    return economy != null;
  }
}
