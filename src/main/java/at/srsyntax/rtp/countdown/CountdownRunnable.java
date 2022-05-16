package at.srsyntax.rtp.countdown;

import at.srsyntax.rtp.RTPPlugin;
import at.srsyntax.rtp.api.Message;
import at.srsyntax.rtp.config.MessageConfig;
import at.srsyntax.rtp.config.PluginConfig;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
public class CountdownRunnable implements Runnable {

  private final RTPPlugin plugin;
  private final CountdownHandlerImpl handler;
  private final Player player;

  private int time;

  private final Location location;

  public CountdownRunnable(RTPPlugin plugin, CountdownHandlerImpl handler, Player player, int time) {
    this.plugin = plugin;
    this.handler = handler;
    this.player = player;
    this.time = time;
    this.location = player.getLocation();
  }

  @Override
  public void run() {
    if (time == 0) {
      handler.finish();
      return;
    }

    final PluginConfig config = plugin.getConfig();
    final MessageConfig messageConfig = config.getMessage();

    if (checkLocation()) {
      new Message(messageConfig.getCountdown(), config).send(player);
      time--;
    } else {
      new Message(messageConfig.getCountdownCanceled(), config).send(player);
      handler.cancel(true);
    }
  }

  private boolean checkLocation() {
    return player.getLocation().distance(location) < .5D;
  }

}
