package at.srsyntax.rtp.api.location;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
// Source: https://gist.github.com/SrSyntaxAT/14bdaa34201cb8b61a00e8bd1ef8c585
public class LocationRandomizer {

  private static final List<Material> DEFAULT_SPAWN_BLACKLIST = Arrays.asList(Material.AIR, Material.LAVA, Material.WATER);

  private final World world;
  private final int size;

  private final List<Material> spawnBlacklist = new ArrayList<>();

  private int meanCoordinateX = 0, meanCoordinateZ = 0;

  public LocationRandomizer(World world, int size) {
    if (size < 10) throw new IllegalArgumentException("the size must be greater than 10 blocks");
    this.world = world;
    this.size = size;
  }

  public LocationRandomizer addBlacklist(Material material) {
    spawnBlacklist.add(material);
    return this;
  }

  public LocationRandomizer addBlacklist(List<Material> list) {
    spawnBlacklist.addAll(list);
    return this;
  }

  public LocationRandomizer addBlacklist(Material... materials) {
    return addBlacklist(Arrays.asList(materials));
  }

  public LocationRandomizer meanCoordinate(int x, int z) {
    meanCoordinateX = x;
    meanCoordinateZ = z;
    return this;
  }

  public Location random() {
    final List<Material> blacklist = getBlacklist();
    final boolean nether = world.getEnvironment() == World.Environment.NETHER;
    int x, y, z;

    do {
      x = random(meanCoordinateX);
      z = random(meanCoordinateZ);
      y = world.getHighestBlockYAt(x, z);


      if (nether)
        y = getYInNether(world, x, y, z);
      else if (y != 0)
        y++;

      if (!isYValid(blacklist, world.getBlockAt(x, y-1, z).getType()))
        y = 0;

    } while (y == 0);

    return new Location(world, x, y, z);
  }

  private int getYInNether(World world, int x, int y, int z) {
    while (y != 0) {
      y = findBlockAtY(world, x, y, z, true);

      y--;
      final Block block = world.getBlockAt(x, y, z);
      if (!block.getType().isAir())
        continue;

      y = findBlockAtY(world, x, y, z, false) + 1;
      break;
    }
    return y;
  }

  private int findBlockAtY(World world, int x, int y, int z, boolean air) {
    Block block;
    do {
      block = world.getBlockAt(x, y, z);
      if (block.getType().isAir() == air) break;
      y--;
    } while (y > 0);
    return y;
  }

  private boolean isYValid(List<Material> blacklist, Material material) {
    return !blacklist.contains(material);
  }

  private int random(int center) {
    final int size = this.size/2;
    return ThreadLocalRandom.current().nextInt(center - size, center + size);
  }

  private List<Material> getBlacklist() {
    if (spawnBlacklist.isEmpty())
      return DEFAULT_SPAWN_BLACKLIST;
    return spawnBlacklist;
  }

  public World getWorld() {
    return world;
  }

  public int getSize() {
    return size;
  }

  public List<Material> getSpawnBlacklist() {
    return spawnBlacklist;
  }

  public int getMeanCoordinateX() {
    return meanCoordinateX;
  }

  public int getMeanCoordinateZ() {
    return meanCoordinateZ;
  }
}
