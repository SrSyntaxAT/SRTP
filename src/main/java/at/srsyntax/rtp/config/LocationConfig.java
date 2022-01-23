package at.srsyntax.rtp.config;

import at.srsyntax.rtp.api.teleport.Cooldown;
import at.srsyntax.rtp.api.teleport.Radius;
import at.srsyntax.rtp.api.teleport.TeleportLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

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
public class LocationConfig implements TeleportLocation {

  private final String name, world;
  private final double x, z, price;
  private final Radius radius;
  private final short cooldown, countdown;

  public LocationConfig(String name, String world, double x, double z, double price, Radius radius, short cooldown, short countdown) {
    this.name = name;
    this.world = world;
    this.x = x;
    this.z = z;
    this.price = price;
    this.radius = radius;
    this.cooldown = cooldown;
    this.countdown = countdown;
  }

  public Location toLocation() {
    return new Location(
        Bukkit.getWorld(world),
        x, 0D, z,
        90f, 0f
    );
  }
  
  public String getName() {
    return name;
  }
  
  public Radius getRadius() {
    return radius;
  }
  
  public short getCountdown() {
    return countdown;
  }

  @Override
  public double getPrice() {
    return price;
  }

  @Override
  public Cooldown getCooldown() {
    return new Cooldown(name, cooldown);
  }
}
