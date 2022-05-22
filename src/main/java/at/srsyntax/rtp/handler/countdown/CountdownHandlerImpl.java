package at.srsyntax.rtp.handler.countdown;

import at.srsyntax.rtp.RTPPlugin;
import at.srsyntax.rtp.api.handler.countdown.CountdownCallback;
import at.srsyntax.rtp.api.handler.countdown.CountdownException;
import at.srsyntax.rtp.api.handler.countdown.CountdownHandler;
import at.srsyntax.rtp.api.event.countdown.CountdownCanceledEvent;
import at.srsyntax.rtp.api.event.countdown.CountdownFinishedEvent;
import at.srsyntax.rtp.api.event.countdown.CountdownStartEvent;
import at.srsyntax.rtp.api.handler.HandlerException;
import at.srsyntax.rtp.api.location.TeleportLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

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

  public void finish() {
    if (task == null || task.isCancelled()) return;
    cancel(false);
    callback.done();
    Bukkit.getPluginManager().callEvent(new CountdownFinishedEvent(this));
  }

  public void cancel(boolean event) {
    if (task == null || task.isCancelled()) return;
    plugin.getCountdownHandlerMap().remove(player);
    task.cancel();
    if (event) Bukkit.getPluginManager().callEvent(new CountdownCanceledEvent(this));
  }

  @Override
  public boolean running() {
    return task != null && !task.isCancelled();
  }

  @Override
  public boolean hasActivCountdown() {
    return RTPPlugin.getApi().hasActivCountdown(player);
  }

  @Override
  public @Nullable CountdownHandler getActivCountdown() {
    return RTPPlugin.getApi().getCountdownHandler(player);
  }

  @Override
  public void handle() throws HandlerException {
    if (task != null || hasActivCountdown())
      throw new CountdownException(plugin.getConfig().getMessage().getCountdownAlreadyActive());
    plugin.getCountdownHandlerMap().put(player, this);
    Bukkit.getPluginManager().callEvent(new CountdownStartEvent(this));
    final Runnable runnable = new CountdownRunnable(plugin, this, player, teleportLocation.getCountdown());
    task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0L, 20L);
  }

  @Override
  public boolean canBypass() {
    return canBypass(player, "syntaxrtp.countdown.bypass", teleportLocation.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CountdownHandlerImpl that = (CountdownHandlerImpl) o;

    if (plugin != null ? !plugin.equals(that.plugin) : that.plugin != null) return false;
    if (teleportLocation != null ? !teleportLocation.equals(that.teleportLocation) : that.teleportLocation != null)
      return false;
    if (player != null ? !player.equals(that.player) : that.player != null) return false;
    if (callback != null ? !callback.equals(that.callback) : that.callback != null) return false;
    return task != null ? task.equals(that.task) : that.task == null;
  }

  @Override
  public int hashCode() {
    int result = plugin != null ? plugin.hashCode() : 0;
    result = 31 * result + (teleportLocation != null ? teleportLocation.hashCode() : 0);
    result = 31 * result + (player != null ? player.hashCode() : 0);
    result = 31 * result + (callback != null ? callback.hashCode() : 0);
    result = 31 * result + (task != null ? task.hashCode() : 0);
    return result;
  }
}
