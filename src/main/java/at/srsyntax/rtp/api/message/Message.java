package at.srsyntax.rtp.api.message;

import at.srsyntax.rtp.SyntaxRTP;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

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
public class Message {

  private Player target;

  private final String message;
  private final MessageType type;

  private String prefix;

  public Message(Player target, String message, MessageType type) {
    this.target = target;
    this.message = message;
    this.type = type;
  }

  public Message(Player target, String[] message) {
    this(target, message[message.length-1], MessageType.valueOf(message[message.length-2].toUpperCase()));
  }

  public Message(Player target, String message) {
    this(target, message.split(";"));
  }

  public Message(String[] messages) {
    this(null, messages);
  }

  public Message prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public Message target(Player target) {
    this.target = target;
    return this;
  }

  public void send() {
    send(null);
  }

  public void send(Map<String, String> replaces) {
    switch (type) {
      case CHAT:
        target.sendMessage(replace(SyntaxRTP.getAPI().isUsingPrefix() ? prefix : null, message, replaces));
        break;
      case TITLE:
        target.sendTitle(prefix == null ? "" : replace(prefix, null), replace(message, replaces));
        break;
      case ACTIONBAR:
        target.sendActionBar(replace(SyntaxRTP.getAPI().isUsingPrefix() ? prefix : null, message, replaces));
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  public static String replace(String message, Map<String, String> replaces) {
    if (replaces == null) replaces = new HashMap<>();

    for (MessageType type : MessageType.values())
      message = message.replace(type.name() + ";", "");
    
    for (Map.Entry<String, String> entry : replaces.entrySet())
      message = message.replace(entry.getKey(), entry.getValue());

    return message.replace("&", "§");
  }

  public static String replace(String prefix, String message, Map<String, String> replaces) {
    return (SyntaxRTP.getAPI().isUsingPrefix() ? (prefix == null ? "" : replace(prefix, null)) : "") + replace(message, replaces);
  }

  @Override
  public String toString() {
    return "Message{" +
        "message='" + message + '\'' +
        ", type=" + type +
        ", prefix='" + prefix + '\'' +
        '}';
  }
}
