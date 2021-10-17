package at.srsyntax.rtp.api;

import at.srsyntax.rtp.api.countdown.Callback;
import at.srsyntax.rtp.api.countdown.Countdown;
import at.srsyntax.rtp.api.exception.TeleportException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/*
 * MIT License
 *
 * Copyright (c) 2021 Marcel Haberl
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


  boolean teleport(@NotNull Player target) throws TeleportException;
  boolean teleport(@NotNull Player target, int cooldown, int countdown) throws TeleportException;
  boolean teleport(@NotNull Player target, @Nullable Location location) throws TeleportException;
  boolean teleport(@NotNull Player target, @Nullable Location location, int cooldown, int countdown) throws TeleportException;

  CompletableFuture<Boolean> teleportAsync(@NotNull Player target) throws TeleportException;
  CompletableFuture<Boolean> teleportAsync(@NotNull Player target, int cooldown, int countdown) throws TeleportException;
  CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @Nullable Location location) throws TeleportException;
  CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @Nullable Location location, int cooldown, int countdown) throws TeleportException;

  Countdown createCountdown(int time, @NotNull Callback callback);

  long hasCooldown(@NotNull Player player);
  Location randomLocation(@NotNull Location location, int minRadius, int radius);
}
