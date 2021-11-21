package at.srsyntax.rtp;

import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.api.countdown.Callback;
import at.srsyntax.rtp.api.countdown.Countdown;
import at.srsyntax.rtp.api.event.PlayerRandomTeleportEvent;
import at.srsyntax.rtp.api.exception.LocationNotFound;
import at.srsyntax.rtp.api.message.Message;
import at.srsyntax.rtp.api.exception.TeleportException;
import at.srsyntax.rtp.api.message.MessageType;
import at.srsyntax.rtp.api.teleport.Cooldown;
import at.srsyntax.rtp.api.teleport.Radius;
import at.srsyntax.rtp.api.teleport.TeleportLocation;
import at.srsyntax.rtp.command.RTPCommand;
import at.srsyntax.rtp.config.LocationConfig;
import at.srsyntax.rtp.config.exception.ConfigLoadException;
import at.srsyntax.rtp.config.MessageConfig;
import at.srsyntax.rtp.config.PermissionConfig;
import at.srsyntax.rtp.config.PluginConfig;
import at.srsyntax.rtp.impl.CountdownImpl;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
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
import java.util.Locale;
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
  
  private boolean usePrefix = true;

  public static final String METADATA_COOLDOWN_KEY = "srtpc:#";

  @Override
  public void onEnable() {
    try {
      instance = this;

      pluginConfig = loadConfig();
      registerCommand();

    } catch (Exception exception) {
      exception.printStackTrace();
      Bukkit.getPluginManager().disablePlugin(this);
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

  private void checkCooldown(Player target, Cooldown cooldown, MessageConfig messageConfig) {
    if (cooldown != null) {
      final long cooldownTime = TimeUnit.MILLISECONDS.toSeconds(hasCooldown(target, cooldown));
      if (cooldown.getTime() > 0 && cooldownTime > 0) {
        final String unit = cooldownTime > 1 ? messageConfig.getSeconds() : messageConfig.getSecond();
        final Map<String, String> replaces = Collections.singletonMap("<time>", cooldownTime + " " + unit);
        throw new TeleportException(Message.replace(messageConfig.getTeleportCooldownError(), replaces));
      }
    }
  }

  private Callback createCallback(Player target, TeleportLocation location) {
    return new Callback() {
      @Override
      public void done(Player player, TeleportLocation location) {
        Bukkit.getScheduler().runTask(SyntaxRTP.instance, () -> {
          final Location oldLocation = player.getLocation();
          Bukkit.getPluginManager().callEvent(new PlayerRandomTeleportEvent(player, oldLocation, location.toLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN));
          
          if (pluginConfig.isPaperAsync()) {
            try {
              player.teleportAsync(location.toLocation()).get();
            } catch (InterruptedException | ExecutionException e) {
              e.printStackTrace();
            }
          }
          else
            player.teleport(location.toLocation());
          player.setMetadata(METADATA_COOLDOWN_KEY.replace("#", location.getName()), new FixedMetadataValue(SyntaxRTP.instance, System.currentTimeMillis()));
        });
      }

      @Override
      public Player getPlayer() {
        return target;
      }

      @Override
      public TeleportLocation getLocation() {
        return location;
      }
    };
  }
  
  @Override
  public boolean teleport(@NotNull Player target, @NotNull Location location, @NotNull Radius radius) throws TeleportException {
    return teleport(target, location, radius, 0);
  }
  
  @Override
  public boolean teleport(@NotNull Player target, @NotNull Location location, @NotNull Radius radius, int countdown) throws TeleportException {
    return teleport(target, location, radius, null, countdown);
  }
  
  @Override
  public boolean teleport(@NotNull Player target, @NotNull Location location, @NotNull Radius radius, Cooldown cooldown, int countdown) throws TeleportException {
    final TeleportLocation teleportLocation = constructLocation(
      cooldown == null ? "default" : cooldown.getKey(),
      location, radius,
      cooldown == null ? (short) 0 : cooldown.getTime(), (short) 0
    );
    
    return teleport(target, teleportLocation);
  }
  
  @Override
  public boolean teleport(@NotNull Player target, @NotNull TeleportLocation location) throws TeleportException {
    final MessageConfig messageConfig = pluginConfig.getMessages();
  
    checkCooldown(target, location.getCooldown(), messageConfig);
    final Callback callback = createCallback(target, location);
  
    if (location.getCountdown() > 0) {
      final Countdown countdown = createCountdown(location.getCountdown(), callback);
      registerMessagesForCountdown(countdown, messageConfig.getTeleportCountdown(), messageConfig.getPrefix());
      countdown.start();
      return true;
    } else {
      callback.done(target, location);
      return false;
    }
  }
  
  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @NotNull Location location, @NotNull Radius radius) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location, radius));
  }
  
  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @NotNull Location location, @NotNull Radius radius, int countdown) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location, radius, countdown));
  }
  
  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @NotNull Location location, @NotNull Radius radius, Cooldown cooldown, int countdown) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location, radius, cooldown, countdown));
  }
  
  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @NotNull TeleportLocation location) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location));
  }
  
  @Override
  public Countdown createCountdown(int time, @NotNull Callback callback) {
    return new CountdownImpl(time, callback, this);
  }
  
  @Override
  public TeleportLocation constructLocation(@NotNull String name, @NotNull Location location, @NotNull Radius radius, short cooldown, short countdown) {
    return constructLocation(name, location.getWorld().getName(), location.getX(), location.getZ(), radius, cooldown, countdown);
  }
  
  @Override
  public TeleportLocation constructLocation(@NotNull String name, @NotNull String world, double x, double z, @NotNull Radius radius, short cooldown, short countdown) {
    return new LocationConfig(name, world, x, z, radius, cooldown, countdown);
  }
  
  @Override
  public TeleportLocation getLocation(@NotNull String name) throws LocationNotFound {
    for (LocationConfig config : pluginConfig.getLocations()) {
      if (config.getName().equalsIgnoreCase(name))
        return config;
    }
    
    throw new LocationNotFound("Location " + name + " not found!");
  }
  
  @Override
  public TeleportLocation getCommandAliasLocation(@NotNull String command) throws LocationNotFound {
    final String name = filterAliases(command);
    
    if (name == null)
      throw new LocationNotFound("Location not found!");
    
    return getLocation(name);
  }
  
  private String filterAliases(String command) {
    for (String alias : pluginConfig.getAliases()) {
      if (alias.toLowerCase().startsWith(command.toLowerCase())) {
        final String[] splited = alias.split(" ");
      
        if (splited.length >= 2)
          return splited[1];
        else
          return pluginConfig.getDefaultLocation();
      }
    }
    return null;
  }
  
  private boolean hasBypassPermission(Player player, String key) {
    return player.hasPermission(pluginConfig.getPermissions().getTeleportCooldownBypass())
      || player.hasPermission(pluginConfig.getPermissions().getTeleportCooldownBypass().replace("*", key));
  }
  
  @Override
  public long hasCooldown(@NotNull Player player, @NotNull Cooldown cooldown) {
    if (cooldown.getTime() > 0 && !hasBypassPermission(player, cooldown.getKey())) {
      final String key = METADATA_COOLDOWN_KEY.replace("#", cooldown.getKey());
      if (!player.hasMetadata(key)) return 0;
  
      final List<MetadataValue> list = player.getMetadata(key);
      if (list.isEmpty()) return 0;
  
      final long value = list.get(0).asLong(), result;
      result = value + TimeUnit.SECONDS.toMillis(cooldown.getTime()) - System.currentTimeMillis();
      return result < 0 ? 0 : result;
    }
    return 0;
  }
  
  @Override
  public long hasCooldown(@NotNull Player player, @NotNull TeleportLocation location) {
    return hasCooldown(player, location.getCooldown());
  }
  
  @Override
  public Location randomLocation(@NotNull Location center, @NotNull Radius radius) {
    boolean nether = center.getWorld().getEnvironment() == World.Environment.NETHER;
    int x, y, z;

    do {
      x = random(center.getBlockX(), radius.getMin(), radius.getMax());
      z = random(center.getBlockZ(), radius.getMin(), radius.getMax());
      y = center.getWorld().getHighestBlockYAt(x, z);

      if (nether) {

        while (y != 0) {
          y = findBlockAtY(center, x, y, z, true);

          y--;
          final Block block = getBlock(center, x, y, z);
          if (!block.getType().isAir())
            continue;

          y = findBlockAtY(center, x, y, z, false) + 1;
          break;
        }
      } else if (y != 0) {
        y++;
      }
    } while (y == 0);

    return new Location(center.getWorld(), x, y, z, center.getYaw(), center.getPitch());
  }
  
  @Override
  public boolean isUsingPrefix() {
    return usePrefix;
  }
  
  @Override
  public void setUsingPrefix(boolean using) {
    this.usePrefix = using;
  }
  
  private Block getBlock(Location location, int x, int y, int z) {
    return location.getWorld().getBlockAt(x, y, z);
  }

  private int findBlockAtY(Location location, int x, int y, int z, boolean air) {
    Block block;
    do {
      block = getBlock(location, x, y, z);
      if (block.getType().isAir() == air) break;
      y--;
    } while (y > 0);
    return y;
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
  
  public void sync(Runnable runnable) {
    Bukkit.getScheduler().runTask(this, runnable);
  }

  private PluginConfig loadConfig() throws ConfigLoadException {
    if (!getDataFolder().exists()) getDataFolder().mkdirs();
    return PluginConfig.load(
        new PluginConfig(
            false,
            true,
            new String[]{"randomteleport", "rtp", "farming farming_world"},
          "farming_world",
          new LocationConfig[]{
              new LocationConfig(
                "farming_world",
                "world", 0D, 0D,
                new Radius(150, 3000),
                (short) 120, (short) 5
              ),
              new LocationConfig(
                "farming_world_2",
                "world", 6000D, 6000D,
                new Radius(150, 3000),
                (short) 120, (short) 5
              )
            },
            new PermissionConfig(
                "srtp.command.teleport",
                "srtp.command.teleport.other",
                "srtp.command.teleport.countdown.bypass.*",
                "srtp.command.teleport.cooldown.bypass.*"
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
