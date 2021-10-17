package at.srsyntax.rtp.impl;

import at.srsyntax.rtp.SyntaxRTP;
import at.srsyntax.rtp.api.countdown.Callback;
import at.srsyntax.rtp.api.countdown.Countdown;
import at.srsyntax.rtp.api.message.Message;
import at.srsyntax.rtp.config.MessageConfig;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ExecutionException;

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
public class CountdownImpl implements Countdown {

  private final SyntaxRTP plugin;

  private int time, task;
  private final Callback callback;

  private final Map<Integer, LinkedList<Message>> map = new HashMap<>();

  public CountdownImpl(int time, Callback callback, SyntaxRTP plugin) {
    this.time = time;
    this.callback = callback;
    this.plugin = plugin;
  }

  @Override
  public Countdown addMessage(int time, Message message) {
    if (!map.containsKey(time))
      map.put(time, new LinkedList<>());
    map.get(time).add(message);
    return this;
  }

  @Override
  public int getTime() {
    return time;
  }

  @Override
  public void start() {
    final MessageConfig config = plugin.getPluginConfig().getMessages();
    this.task = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, () -> {
      try {
        if (map.containsKey(time)) {
          final List<Message> messages = map.get(time);

          messages.forEach(message -> {
            final String time = this.time + " " + (this.time > 1 ? config.getSeconds() : config.getSecond());
            message.target(callback.getPlayer()).send(Collections.singletonMap("<time>", time));
          });
        }

        if (time == 0) {
          callback.done(callback.getPlayer(), callback.getLocation());
          cancel();
        }

        time--;
      } catch (Exception e) {
        e.printStackTrace();
        cancel();
        new Message(callback.getPlayer(), config.getTeleportError()).prefix(config.getPrefix()).send();
      }
    }, 0, 20);
  }

  @Override
  public String toString() {
    return "CountdownImpl{" +
        "plugin=" + plugin +
        ", time=" + time +
        ", task=" + task +
        ", callback=" + callback +
        ", map=" + map +
        '}';
  }

  @Override
  public void cancel() {
    Bukkit.getScheduler().cancelTask(task);
  }
}
