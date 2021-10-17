package at.srsyntax.rtp.config;

import at.srsyntax.rtp.config.exception.ConfigLoadException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

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
public class PluginConfig {

  private final boolean paperAsync;
  private final String[] aliases;
  private final LocationConfig teleportCenter;
  private final int radius, minRadius;
  private final short cooldown, countdown;
  private final PermissionConfig permissions;
  private final MessageConfig messages;

  public PluginConfig(boolean paperAsync, String[] aliases, LocationConfig teleportCenter, int minRadius, int radius, short cooldown, short countdown, PermissionConfig permissions, MessageConfig messages) {
    this.paperAsync = paperAsync;
    this.aliases = aliases;
    this.teleportCenter = teleportCenter;
    this.minRadius = minRadius;
    this.radius = radius;
    this.cooldown = cooldown;
    this.countdown = countdown;
    this.permissions = permissions;
    this.messages = messages;
  }

  public static PluginConfig load(PluginConfig defaultConfig, File file) throws ConfigLoadException {
    try {
      if (file.exists()) return new Gson().fromJson(new FileReader(file), PluginConfig.class);
      file.createNewFile();

      Files.write(
          file.toPath(),
          Arrays.asList(defaultConfig.toString().split("\n")),
          StandardCharsets.UTF_8
      );

      return defaultConfig;
    } catch (IOException exception) {
      throw new ConfigLoadException("An error has occurred while loading the Config!", exception);
    }
  }

  @Override
  public String toString() {
    return new GsonBuilder().setPrettyPrinting().create().toJson(this);
  }

  public boolean isPaperAsync() {
    return paperAsync;
  }

  public String[] getAliases() {
    return aliases;
  }

  public LocationConfig getTeleportCenter() {
    return teleportCenter;
  }

  public int getMinRadius() {
    return minRadius;
  }

  public int getRadius() {
    return radius;
  }

  public short getCooldown() {
    return cooldown;
  }

  public short getCountdown() {
    return countdown;
  }

  public PermissionConfig getPermissions() {
    return permissions;
  }

  public MessageConfig getMessages() {
    return messages;
  }
}
