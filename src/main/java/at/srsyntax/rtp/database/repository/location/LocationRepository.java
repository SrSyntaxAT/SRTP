package at.srsyntax.rtp.database.repository.location;

import at.srsyntax.rtp.api.location.LocationCache;
import at.srsyntax.rtp.api.location.TeleportLocation;
import at.srsyntax.rtp.database.SQLRepository;

import java.sql.SQLException;
import java.util.List;

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
public interface LocationRepository extends SQLRepository {

  List<LocationCache> getLocations(TeleportLocation teleportLocation) throws SQLException;
  void removeLocation(LocationCache cache) throws SQLException;
  void removeLocations(TeleportLocation teleportLocation) throws SQLException;
  void addLocation(TeleportLocation teleportLocation, LocationCache cache) throws SQLException;

}
