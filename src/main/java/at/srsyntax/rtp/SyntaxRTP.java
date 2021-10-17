package at.srsyntax.rtp;

import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.api.countdown.Callback;
import at.srsyntax.rtp.api.countdown.Countdown;
import at.srsyntax.rtp.api.message.Message;
import at.srsyntax.rtp.api.exception.InvalidWorldTypeException;
import at.srsyntax.rtp.api.exception.TeleportException;
import at.srsyntax.rtp.api.message.MessageType;
import at.srsyntax.rtp.command.RTPCommand;
import at.srsyntax.rtp.config.LocationConfig;
import at.srsyntax.rtp.config.exception.ConfigLoadException;
import at.srsyntax.rtp.config.MessageConfig;
import at.srsyntax.rtp.config.PermissionConfig;
import at.srsyntax.rtp.config.PluginConfig;
import at.srsyntax.rtp.impl.CountdownImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldType;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
public class SyntaxRTP extends JavaPlugin implements API {

  private static SyntaxRTP instance;

  private PluginConfig pluginConfig;
  private Location teleportCenter;

  public static final String METADATA_KEY = "srtpCooldown";

  @Override
  public void onEnable() {
    try {
      instance = this;

      pluginConfig = loadConfig();
      teleportCenter = pluginConfig.getTeleportCenter().toLocation();

      registerCommand();

    } catch (Exception exception) {
      exception.printStackTrace();
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public boolean teleport(@NotNull Player target) throws TeleportException {
    return teleport(
        target,
        pluginConfig.getCooldown(),
        target.hasPermission(pluginConfig.getPermissions().getTeleportCountdownBypass()) ? 0 : pluginConfig.getCountdown()
    );
  }

  @Override
  public boolean teleport(@NotNull Player target, int cooldown, int countdown) throws TeleportException {
    return teleport(target, null, cooldown, countdown);
  }

  @Override
  public boolean teleport(@NotNull Player target, @Nullable Location location) throws TeleportException {
    return teleport(target, location, pluginConfig.getCooldown(), pluginConfig.getCountdown());
  }

  @Override
  public boolean teleport(@NotNull Player target, @Nullable Location location, int cooldown, int countdownTime) throws TeleportException {
    final MessageConfig messageConfig = pluginConfig.getMessages();

    checkCooldown(target, cooldown, messageConfig);
    location = checkLocation(location);
    final Callback callback = createCallback(target, location);

    if (countdownTime > 0) {
      final Countdown countdown = createCountdown(countdownTime, callback);
      registerMessagesForCountdown(countdown, messageConfig.getTeleportCountdown(), messageConfig.getPrefix());
      countdown.start();
      return true;
    } else {
      callback.done(target, location);
      return false;
    }
  }

  private void registerMessagesForCountdown(Countdown countdown, String[] messages, String prefix) {
    for (String line : messages) {
      final String[] splited = line.split(";"), timestamps = splited[0].split(",");
      final Message message = new Message(splited).prefix(prefix);
      try {
        for (String timestamp : timestamps)
          countdown.addMessage(Integer.parseInt(timestamp), message);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  private void checkCooldown(Player target, int cooldown, MessageConfig messageConfig) {
    final long cooldownTime = TimeUnit.MILLISECONDS.toSeconds(hasCooldown(target));
    if (cooldown > 0 && cooldownTime > 0) {
      final String unit = cooldownTime > 1 ? messageConfig.getSeconds() : messageConfig.getSecond();
      final Map<String, String> replaces = Collections.singletonMap("<time>", cooldownTime + " " + unit);
      throw new TeleportException(Message.replace(messageConfig.getTeleportCooldownError(), replaces));
    }
  }

  private Location checkLocation(Location location) {
    if (location == null)
      location = randomLocation(teleportCenter, pluginConfig.getMinRadius(), pluginConfig.getRadius());
    return location;
  }

  private Callback createCallback(Player target, Location location) {
    return new Callback() {
      @Override
      public void done(Player player, Location location) {
        Bukkit.getScheduler().runTask(SyntaxRTP.instance, () -> {
          if (pluginConfig.isPaperAsync()) {
            try {
              player.teleportAsync(location).get();
            } catch (InterruptedException | ExecutionException e) {
              e.printStackTrace();
            }
          }
          else
            player.teleport(location);
          player.setMetadata(METADATA_KEY, new FixedMetadataValue(SyntaxRTP.instance, System.currentTimeMillis()));
        });
      }

      @Override
      public Player getPlayer() {
        return target;
      }

      @Override
      public Location getLocation() {
        return location;
      }
    };
  }

  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target));
  }

  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, int cooldown, int countdown) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, cooldown, countdown));
  }

  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @Nullable Location location) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location));
  }

  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @Nullable Location location, int cooldown, int countdown) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location, cooldown, countdown));
  }

  @Override
  public Countdown createCountdown(int time, @NotNull Callback callback) {
    return new CountdownImpl(time, callback, this);
  }

  @Override
  public long hasCooldown(@NotNull Player player) {
    if (pluginConfig.getCooldown() > 0 && !player.hasPermission(pluginConfig.getPermissions().getTeleportCooldownBypass())) {
      if (!player.hasMetadata(METADATA_KEY)) return 0;

      final List<MetadataValue> list = player.getMetadata(METADATA_KEY);
      if (list.isEmpty()) return 0;

      final long value = list.get(0).asLong(), result;
      result = value + TimeUnit.SECONDS.toMillis(pluginConfig.getCooldown()) - System.currentTimeMillis();
      return result < 0 ? 0 : result;
    }
    return 0;
  }

  @Override
  public Location randomLocation(Location center, int minRadius, int radius) {
    final WorldType worldType = center.getWorld().getWorldType();
    if (worldType != null && worldType != WorldType.NORMAL) throw new InvalidWorldTypeException();

    int x, y, z;

    x = random(center.getBlockX(), minRadius, radius);
    z = random(center.getBlockZ(), minRadius, radius);
    y = center.getWorld().getHighestBlockYAt(x, z) + 1;

    return new Location(center.getWorld(), x, y, z, center.getYaw(), center.getPitch());
  }

  private int random(int current, int min, int radius) {
    boolean finished = false;
    int result;

    do {
      result = ThreadLocalRandom.current().nextInt(current - radius, current + radius);
      if (allowedResult(result, current, min, radius))
        finished = true;
    } while (!finished);

    return result;
  }

  private boolean allowedResult(int result, int current, int min, int radius) {
    return isHigherAllowed(result, current, min, radius) || isLowerAllowed(result,current, min, radius);
  }

  private boolean isHigherAllowed(int result, int current, int min, int radius) {
    return (result > current && result > current + min) && result <= radius;
  }

  private boolean isLowerAllowed(int result, int current, int min, int radius) {
    return (result < current && result < current - min) && result >= radius;
  }

  private void registerCommand() throws NoSuchFieldException, IllegalAccessException {
    final Field field = SimplePluginManager.class.getDeclaredField("commandMap");
    field.setAccessible(true);
    final SimpleCommandMap commandMap = (SimpleCommandMap) field.get(Bukkit.getPluginManager());
    commandMap.register(getName(), new RTPCommand(this, pluginConfig));
    field.setAccessible(false);
  }

  private PluginConfig loadConfig() throws ConfigLoadException {
    if (!getDataFolder().exists()) getDataFolder().mkdirs();
    return PluginConfig.load(
        new PluginConfig(
            false,
            new String[]{"randomteleport", "rtp"},
            new LocationConfig("world", 0D, 0D, 0D),
            150,
            5000,
            (short) 120,
            (short) 5,
            new PermissionConfig(
                "srtp.command.teleport",
                "srtp.command.teleport.other",
                "srtp.command.teleport.countdown.bypass",
                "srtp.command.teleport.cooldown.bypass"
            ),
            new MessageConfig(
                new String[]{
                    "5,3,1;" + MessageType.TITLE.name() + ";&eYou will teleport in &6<time>&e.",
                    "0;" + MessageType.TITLE.name() + ";&aYou have been teleported!"
                },
                "&7[&bSyntaxRTP&7] &r",
                MessageType.ACTIONBAR.name() + ";&aYou have been teleported!",
                MessageType.CHAT.name() + ";&aYou have teleported &e<player>&a.",
                MessageType.ACTIONBAR.name() + ";&cYou can use the command again only in &e<time>&c!",
                MessageType.CHAT.name() + ";&cYou are not authorized to do that! :/",
                MessageType.CHAT.name() + ";&cAn error occurred while teleporting! :c",
                MessageType.CHAT.name() + ";&cPlayer was not found!",
                "second", "seconds"
            )
        ),
        new File(getDataFolder(), "config.json")
    );
  }

  public static API getAPI() {
    return instance;
  }

  public PluginConfig getPluginConfig() {
    return pluginConfig;
  }
}
