package at.srsyntax.rtp.cooldown;

import at.srsyntax.rtp.RTPPlugin;
import at.srsyntax.rtp.api.cooldown.CooldownHandler;
import at.srsyntax.rtp.api.location.TeleportLocation;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

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
@AllArgsConstructor
public class CooldownHandlerImpl implements CooldownHandler {

  public static final String METADATA_KEY = "srtp:cd:";

  private final RTPPlugin plugin;
  private final TeleportLocation teleportLocation;
  private final Player player;

  @Override
  public boolean hasCooldown() {
    if (hasBypassPermission() || player.hasMetadata(getMetadataKey())) return false;
    final long end = player.getMetadata(getMetadataKey()).get(0).asLong();
    final boolean activ = System.currentTimeMillis() < end;
    if (!activ) removeCooldown();
    return activ;
  }

  private boolean hasBypassPermission() {
    final String prefix = "syntaxrtp.cooldown.bypass.";
    return player.hasPermission(prefix + "*") || player.hasPermission(prefix + teleportLocation.getName());
  }

  @Override
  public void addCooldown() {
    try {
      final long end = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(teleportLocation.getCooldown());
      player.setMetadata(getMetadataKey(), new FixedMetadataValue(plugin, end));
      plugin.getDatabase().getCooldownRepository().add(player, teleportLocation, end);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void removeCooldown() {
    try {
      player.removeMetadata(getMetadataKey(), plugin);
      plugin.getDatabase().getCooldownRepository().delete(player, teleportLocation);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private String getMetadataKey() {
    return METADATA_KEY + teleportLocation.getName();
  }
}
