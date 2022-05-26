package at.srsyntax.rtp.api;

import at.srsyntax.rtp.config.PluginConfig;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
public class Message {

  private final String message;
  private String prefix;

  private final Map<String, String> replaces = new HashMap<>(Collections.singletonMap("&", "§"));

  public Message(String message, PluginConfig config) {
    this.message = message;
    prefix(config);
  }

  public Message(String message) {
    this.message = message;
  }

  public Message prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public Message prefix(PluginConfig config) {
    if (config.isPrefix())
      prefix = config.getMessage().getPrefix();
    return this;
  }

  public Message add(String key, String value) {
    replaces.put(key, value);
    return this;
  }

  public String replace() {
    return replace(prefix == null ? "" : prefix + " " + message);
  }

  private String replace(String message) {
    for (Map.Entry<String, String> entry : replaces.entrySet()) {
      message = message.replace(entry.getKey(), entry.getValue());
    }
    return message;
  }

  public void send(CommandSender... senders) {
    final String message = replace();
    for (CommandSender sender : senders)
      sender.sendMessage(message);
  }
}
