package at.srsyntax.rtp.handler.countdown;

import at.srsyntax.rtp.RTPPlugin;
import at.srsyntax.rtp.api.handler.countdown.CountdownCallback;
import at.srsyntax.rtp.api.handler.countdown.CountdownHandler;
import at.srsyntax.rtp.api.event.countdown.CountdownCanceledEvent;
import at.srsyntax.rtp.api.event.countdown.CountdownFinishedEvent;
import at.srsyntax.rtp.api.event.countdown.CountdownStartEvent;
import at.srsyntax.rtp.api.handler.HandlerException;
import at.srsyntax.rtp.api.location.TeleportLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

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
public class CountdownHandlerImpl implements CountdownHandler {

  private final RTPPlugin plugin;

  private final TeleportLocation teleportLocation;
  private final Player player;
  private final CountdownCallback callback;

  private BukkitTask task;

  public CountdownHandlerImpl(RTPPlugin plugin, TeleportLocation teleportLocation, Player player, CountdownCallback callback) {
    this.plugin = plugin;
    this.teleportLocation = teleportLocation;
    this.player = player;
    this.callback = callback;
  }

  @Override
  public void start() {
    if (task != null) return;
    Bukkit.getPluginManager().callEvent(new CountdownStartEvent(this));
    final Runnable runnable = new CountdownRunnable(plugin, this, player, teleportLocation.getCountdown());
    task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0L, 20L);
  }

  public void finish() {
    if (task == null || task.isCancelled()) return;
    cancel(false);
    callback.done();
    Bukkit.getPluginManager().callEvent(new CountdownFinishedEvent(this));
  }

  public void cancel(boolean event) {
    if (task == null || task.isCancelled()) return;
    task.cancel();
    if (event) Bukkit.getPluginManager().callEvent(new CountdownCanceledEvent(this));
  }

  @Override
  public boolean running() {
    return task != null && !task.isCancelled();
  }

  @Override
  public void handle() throws HandlerException {
    start();
  }

  @Override
  public boolean canBypass() {
    return canBypass(player, "syntaxrtp.countdown.bypass", teleportLocation.getName());
  }
}
