package at.srsyntax.rtp.api.message;

import at.srsyntax.rtp.SyntaxRTP;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

  private final Map<String, String> replaces = new LinkedHashMap<>();

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

  public Message(String... messages) {
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
        target.spigot().sendMessage(
            ChatMessageType.SYSTEM,
            new TextComponent(replaceChatMessage())
        );
        break;
      case TITLE:
        target.sendTitle(prefix == null ? "" : replace(prefix), replace(), 10, 70, 20);
        break;
      case ACTIONBAR:
        target.spigot().sendMessage(
            ChatMessageType.ACTION_BAR,
            new TextComponent(replaceChatMessage())
        );
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  private String replaceChatMessage() {
    return (prefix == null ? "" : replace(prefix) + " ") + replace(message);
  }

  public Message addReplace(String key, String value) {
    this.replaces.put(key, value);
    return this;
  }

  public Message addReplaces(Map<String, String> replaces) {
    this.replaces.putAll(replaces);
    return this;
  }

  public  String replace() {
    return replace(message);
  }

  private String replace(String message) {
    for (MessageType type : MessageType.values())
      message = message.replace(type.name() + ";", "");

    for (Map.Entry<String, String> entry : replaces.entrySet())
      message = message.replace(entry.getKey(), entry.getValue());

    return message.replace("&", "§");
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
