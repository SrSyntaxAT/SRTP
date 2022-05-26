package at.srsyntax.rtp.api;

import at.srsyntax.rtp.api.handler.cooldown.CooldownHandler;
import at.srsyntax.rtp.api.handler.countdown.CountdownCallback;
import at.srsyntax.rtp.api.handler.countdown.CountdownHandler;
import at.srsyntax.rtp.api.handler.economy.EconomyHandler;
import at.srsyntax.rtp.api.location.TeleportLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public interface API {

  @Nullable TeleportLocation getLocation(@NotNull String name);
  @NotNull TeleportLocation createLocation(@NotNull String name, @NotNull Location location, @Nullable String permission, int size, int countdown, int cooldown, double price);
  @NotNull TeleportLocation createLocation(@NotNull String name, @NotNull Location location, @Nullable String permission, int size, int countdown, int cooldown, double price, @Nullable String[] aliases);
  void deleteLocation(@NotNull String name);
  void deleteLocation(@NotNull TeleportLocation location);
  @NotNull List<TeleportLocation> getLocationsCopy();

  void teleport(@NotNull Player player, @NotNull TeleportLocation location);
  void teleport(@NotNull Player player, @NotNull Location center, int size);
  void teleport(@NotNull Player player, @NotNull World world, int centerX, int centerZ, int size);

  @NotNull CountdownHandler newCountdownHandler(@NotNull TeleportLocation teleportLocation, @NotNull Player player, @NotNull CountdownCallback callback);
  @NotNull CooldownHandler newCooldownHandler(@NotNull TeleportLocation teleportLocation, @NotNull Player player);
  @NotNull EconomyHandler newEconomyHandler(@NotNull TeleportLocation teleportLocation, @NotNull Player player);

  boolean hasActivCountdown(@NotNull Player player);
  @Nullable CountdownHandler getCountdownHandler(@NotNull Player player);

  boolean isVaultSupported();
}
