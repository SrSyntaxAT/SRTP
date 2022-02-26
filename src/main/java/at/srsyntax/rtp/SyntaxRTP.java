package at.srsyntax.rtp;

import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.api.message.MessageType;
import at.srsyntax.rtp.api.teleport.Radius;
import at.srsyntax.rtp.command.RTPCommand;
import at.srsyntax.rtp.command.ReloadCommand;
import at.srsyntax.rtp.config.LocationConfig;
import at.srsyntax.rtp.config.exception.ConfigLoadException;
import at.srsyntax.rtp.config.MessageConfig;
import at.srsyntax.rtp.config.PermissionConfig;
import at.srsyntax.rtp.config.PluginConfig;
import at.srsyntax.rtp.impl.APIImpl;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
public class SyntaxRTP extends JavaPlugin {

  public static final int RESOURCE_ID = 99428;

  private static API api;

  private PluginConfig pluginConfig;

  @Override
  public void onEnable() {
    try {
      api = new APIImpl(this);
      
      api.reload();
      new Metrics(this, 13408);
      ((APIImpl) api).setupEconomy();

      if (!SpigotVersionCheck.check(getDescription().getVersion(), RESOURCE_ID))
        getLogger().warning("A newer version is available!");
    } catch (Exception exception) {
      exception.printStackTrace();
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }


  public void registerCommand() throws NoSuchFieldException, IllegalAccessException {
    final Field field = SimplePluginManager.class.getDeclaredField("commandMap");
    field.setAccessible(true);
    final SimpleCommandMap commandMap = (SimpleCommandMap) field.get(Bukkit.getPluginManager());
    
    final Command command = commandMap.getCommand(getName());
    if (command != null)
      commandMap.getCommands().remove(command);
    
    commandMap.register(getName(), new RTPCommand(this, pluginConfig, getCommandAliases()));
    commandMap.register(getName(), new ReloadCommand(this));
    
    field.setAccessible(false);
  }
  
  private List<String> getCommandAliases() {
    final List<String> list = new ArrayList<>(pluginConfig.getAliases().length);
    
    for (String alias : pluginConfig.getAliases()) {
      final String[] splited = alias.split(" ");
      if (splited.length > 0)
        list.add(splited[0]);
    }
    
    return list;
  }
  
  public void sync(Runnable runnable) {
    Bukkit.getScheduler().runTask(this, runnable);
  }

  public PluginConfig loadConfig() throws ConfigLoadException {
    if (!getDataFolder().exists()) getDataFolder().mkdirs();
    return PluginConfig.load(
        new PluginConfig(
            false,
            true,
            new String[]{"randomteleport", "rtp", "farming farming_world_2"},
          "farming_world",
          new LocationConfig[]{
              new LocationConfig(
                "farming_world",
                "world", 0D, 0D,
                  700D, new Radius(150, 3000),
                (short) 120, (short) 5
              ),
              new LocationConfig(
                "farming_world_2",
                "world", 6000D, 6000D,
                  1000D, new Radius(150, 3000),
                (short) 120, (short) 5
              )
            },
            new PermissionConfig(
                "srtp.command.teleport",
                "srtp.command.teleport.other",
                "srtp.command.teleport.countdown.bypass.*",
                "srtp.command.teleport.cooldown.bypass.*",
              "srtp.command.reload"
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
              MessageType.CHAT.name() + ";&cSyntaxRTP will be reloaded!",
              MessageType.CHAT.name() + ";&4An error occurred during reload!!",
                MessageType.ACTIONBAR.name() + ";&4You do not have enough money!",
                "second", "seconds"
            )
        ),
        new File(getDataFolder(), "config.json")
    );
  }

  public static API getAPI() {
    return api;
  }

  public PluginConfig getPluginConfig() {
    return pluginConfig;
  }

  public void setPluginConfig(PluginConfig pluginConfig) {
    this.pluginConfig = pluginConfig;
  }
}
