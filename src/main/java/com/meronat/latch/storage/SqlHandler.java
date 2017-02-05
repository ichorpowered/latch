/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2017 IchorPowered <https://github.com/IchorPowered>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.meronat.latch.storage;

import com.meronat.latch.Latch;
import com.meronat.latch.entities.Lock;
import com.meronat.latch.enums.LockType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.meronat.latch.Latch.getLogger;

public class SqlHandler {

    private SqlService sql;

    public SqlHandler() {
        createTables();
    }

    private Connection getConnection() throws SQLException {
        if (this.sql == null) {
            this.sql = Sponge.getServiceManager().provide(SqlService.class).get();
        }
        return this.sql.getDataSource("jdbc:h2:" + Latch.getConfigPatch().getParent().toAbsolutePath().toString() + File.separator + "latch").getConnection();
    }

    private void createTables() {
        String createLockTable = "CREATE TABLE IF NOT EXISTS LOCK (" +
                "ID bigint AUTO_INCREMENT, " +
                "OWNER_UUID char(36) NOT NULL, " +
                "LOCK_NAME varchar(25) NOT NULL, " +
                "LOCK_TYPE varchar(15) NOT NULL, " +
                "LOCKED_OBJECT varchar(50) NOT NULL, " +
                "PASSWORD varchar(256) NOT NULL, " +
                "SALT varchar(64) NOT NULL, " +
                "REDSTONE_PROTECT BOOLEAN NOT NULL, " +
                "ACCESSED DATETIME NOT NULL," +
                "PRIMARY KEY(ID), CONSTRAINT UQ_OWNER_NAME UNIQUE (OWNER_UUID, LOCK_NAME) )";

        String createLocationTable = "CREATE TABLE IF NOT EXISTS LOCK_LOCATIONS (" +
                "LOCK_ID bigint, " +
                "WORLD_UUID char(36) NOT NULL, " +
                "X int NOT NULL, " +
                "Y int NOT NULL, " +
                "Z int NOT NULL, " +
                "FOREIGN KEY (LOCK_ID) REFERENCES LOCK(ID), " +
                "PRIMARY KEY (WORLD_UUID, X, Y, Z) )";

        String createPlayerTable = "CREATE TABLE IF NOT EXISTS LOCK_PLAYERS (" +
                "LOCK_ID bigint, " +
                "PLAYER_UUID char(36) NOT NULL, " +
                "FOREIGN KEY (LOCK_ID) REFERENCES LOCK(ID) )";

        try (
                Connection connection = getConnection();
                PreparedStatement psLockTable = connection.prepareStatement(createLockTable);
                PreparedStatement psLocationTable = connection.prepareStatement(createLocationTable);
                PreparedStatement psPlayerTable = connection.prepareStatement(createPlayerTable)
        ) {
            psLockTable.execute();
            psLocationTable.execute();
            psPlayerTable.execute();
        } catch (SQLException e) {
            getLogger().error("Error running SQL createTables:");
            e.printStackTrace();
        }

        try (
                Connection connection = getConnection();
                PreparedStatement add = connection.prepareStatement("ALTER TABLE LOCK ADD COLUMN ACCESSED DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        ) {
            DatabaseMetaData metaData = connection.getMetaData();
            if (!metaData.getColumns(null, null, "LOCK", "ACCESSED").next()) {
                add.execute();
            }
        } catch (SQLException e) {
            getLogger().error("There was a problem adding the ACCESSED column. Please report this to the developers:");
            e.printStackTrace();
        }

    }

    public Optional<Lock> getLockByLocation(Location location) {
        Optional<Lock> lock = Optional.empty();

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT ID, OWNER_UUID, LOCK_NAME, LOCK_TYPE, LOCKED_OBJECT, SALT, PASSWORD, REDSTONE_PROTECT, ACCESSED FROM LOCK_LOCATIONS JOIN LOCK ON (LOCK_LOCATIONS.LOCK_ID = LOCK.ID) "
                                + "WHERE LOCK_LOCATIONS.WORLD_UUID = ? AND LOCK_LOCATIONS.X = ? AND LOCK_LOCATIONS.Y = ? AND LOCK_LOCATIONS.Z = ?")
        ) {
            ps.setObject(1, location.getExtent().getUniqueId());
            ps.setInt(2, location.getBlockX());
            ps.setInt(3, location.getBlockY());
            ps.setInt(4, location.getBlockZ());

            try (
                    ResultSet tempLock = ps.executeQuery()
            ) {
                //Should only be one lock at this location, if any
                if (tempLock.next()) {
                    lock = Optional.of(new Lock(
                            UUID.fromString(tempLock.getString("OWNER_UUID")),
                            tempLock.getString("LOCK_NAME"),
                            LockType.valueOf(tempLock.getString("LOCK_TYPE")),
                            getLockLocationsByID(tempLock.getInt("ID")),
                            tempLock.getString("LOCKED_OBJECT"),
                            tempLock.getBytes("SALT"),
                            tempLock.getString("PASSWORD"),
                            getAbleToAccessByID(tempLock.getInt("ID")),
                            tempLock.getBoolean("REDSTONE_PROTECT"),
                            tempLock.getTimestamp("ACCESSED").toLocalDateTime()));
                }
            }

        } catch (SQLException e) {
            getLogger().error("Error running SQL getLockByLocation: ");
            e.printStackTrace();
        }
        return lock;
    }

    private HashSet<Location<World>> getLockLocationsByID(int id) {
        HashSet<Location<World>> locations = new HashSet<>();

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT WORLD_UUID, X, Y, Z FROM LOCK_LOCATIONS WHERE LOCK_LOCATIONS.LOCK_ID = ?");
        ) {
            ps.setObject(1, id);

            try (
                    ResultSet rs = ps.executeQuery();
            ) {
                while (rs.next()) {
                    Optional<World> world = Sponge.getServer().getWorld(UUID.fromString(rs.getString("WORLD_UUID")));
                    if (world.isPresent()) {
                        locations.add(world.get().getLocation(rs.getInt("X"), rs.getInt("Y"), rs.getInt("Z")));
                    } else {
                        getLogger().error("Error loading location in getLockLocationsByID: " + rs.getString("WORLD_UUID")
                                + " does not exist as a world (ID: " + id + ")");
                    }
                }
            }
        } catch (SQLException e) {
            getLogger().error("Error running SQL getLockLocationsByID:");
            e.printStackTrace();
        }
        return locations;
    }

    private HashSet<UUID> getAbleToAccessByID(int id) {
        HashSet<UUID> players = new HashSet<>();

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT PLAYER_UUID FROM LOCK_PLAYERS WHERE LOCK_PLAYERS.LOCK_ID = ?");
        ) {
            ps.setObject(1, id);

            try (
                    ResultSet rs = ps.executeQuery();
            ) {
                while (rs.next()) {
                    players.add(UUID.fromString(rs.getString("PLAYER_UUID")));
                }
            }
        } catch (SQLException e) {
            getLogger().error("Error running SQL getAbleToAccessByID:");
            e.printStackTrace();
        }
        return players;
    }

    private Optional<Integer> getLockID(Lock lock) {
        return getLockID(lock.getOwner(), lock.getName());
    }

    private Optional<Integer> getLockID(UUID owner, String name) {
        Optional<Integer> id = Optional.empty();

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT ID FROM LOCK WHERE LOCK.OWNER_UUID = ? AND LOCK.LOCK_NAME = ?");
        ) {
            ps.setObject(1, owner);
            ps.setString(2, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = Optional.of(rs.getInt("ID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    private Optional<Integer> getLockID(Location location) {
        Optional<Integer> id = Optional.empty();

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT LOCK_ID FROM LOCK_LOCATIONS WHERE LOCK_LOCATIONS.WORLD_UUID = ? AND LOCK_LOCATIONS.X = ? AND LOCK_LOCATIONS.Y = ? AND LOCK_LOCATIONS.Z = ?");
        ) {
            ps.setObject(1, location.getExtent().getUniqueId());
            ps.setInt(2, location.getBlockX());
            ps.setInt(3, location.getBlockY());
            ps.setInt(4, location.getBlockZ());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = Optional.of(rs.getInt("LOCK_ID"));
                }
            }
        } catch (SQLException e) {
            getLogger().error("Error running SQL getLockID:");
            e.printStackTrace();
        }
        return id;
    }

    public void addLockAccess(Lock thisLock, UUID player) {
        Optional<Integer> id = getLockID(thisLock);

        if (id.isPresent()) {
            try (
                    Connection connection = getConnection();
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO LOCK_PLAYERS(LOCK_ID, PLAYER_UUID) VALUES (?, ?)");
            ) {
                ps.setInt(1, id.get());
                ps.setObject(2, player);
                ps.executeUpdate();
            } catch (SQLException e) {
                getLogger().error("Error addLockAccess for " + thisLock.getName() + ", owner: " + thisLock.getOwner() + ", player: " + player);
                e.printStackTrace();
            }

        }
    }

    public void createLock(Lock lock, HashSet<Location<World>> locations, HashSet<UUID> ableToAccess) {
        try (
                Connection connection = getConnection();
                PreparedStatement psLock = connection.prepareStatement(
                        "INSERT INTO LOCK(OWNER_UUID, LOCK_NAME, LOCK_TYPE, LOCKED_OBJECT, SALT, PASSWORD, REDSTONE_PROTECT, ACCESSED) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement psLocations = connection.prepareStatement("INSERT INTO LOCK_LOCATIONS(LOCK_ID, WORLD_UUID, X, Y, Z) VALUES (?, ?, ?, ?, ?)");
                PreparedStatement psPlayers = connection.prepareStatement("INSERT INTO LOCK_PLAYERS(LOCK_ID, PLAYER_UUID) VALUES (?, ?)");
        ) {
            psLock.setObject(1, lock.getOwner());
            psLock.setString(2, lock.getName());
            psLock.setString(3, lock.getLockType().toString());
            psLock.setString(4, lock.getLockedObject());
            psLock.setBytes(5, lock.getSalt());
            psLock.setString(6, lock.getPassword());
            psLock.setBoolean(7, lock.getProtectFromRedstone());
            psLock.setTimestamp(8, Timestamp.valueOf(lock.getLastAccessed()));

            psLock.executeUpdate();

            try (ResultSet rsLock = psLock.getGeneratedKeys()) {
                if (rsLock.next()) {
                    //If the lock was successfully created, proceed with inserting remaining information
                    //Insert the lock locations
                    for (Location<World> location : locations) {
                        psLocations.setLong(1, rsLock.getLong(1));
                        psLocations.setObject(2, location.getExtent().getUniqueId());
                        psLocations.setInt(3, location.getBlockX());
                        psLocations.setInt(4, location.getBlockY());
                        psLocations.setInt(5, location.getBlockZ());
                        psLocations.addBatch();
                    }
                    psLocations.executeBatch();

                    //Insert the players able to access
                    for (UUID uuid : ableToAccess) {
                        psPlayers.setLong(1, rsLock.getLong(1));
                        psPlayers.setObject(2, uuid);
                        psPlayers.addBatch();
                    }
                    psPlayers.executeBatch();
                } else {
                    throw new SQLException("ResultSet did not return a PK for subsequent inserts.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void deleteLock(Location<World> location, boolean deleteEntireLock) {
        Optional<Integer> lockKey = getLockID(location);
        boolean fullyDelete = deleteEntireLock; //do we fully delete the lock (from all tables)

        if (lockKey.isPresent()) {
            try (Connection connection = getConnection()) {
                //If not forced to delete the entire lock(all locations), only do it if it's the last location
                if (!fullyDelete) {
                    try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM LOCK_LOCATIONS WHERE LOCK_ID = ?")) {
                        ps.setLong(1, lockKey.get());

                        try (
                                ResultSet rs = ps.executeQuery();
                        ) {
                            //if only one row left, fully delete or if no locations left, fully delete
                            fullyDelete = !rs.next() || rs.getLong(1) <= 1;
                        }
                    }
                }

                if (fullyDelete) {
                    try (
                            PreparedStatement locationDelete = connection.prepareStatement("DELETE FROM LOCK_LOCATIONS WHERE LOCK_ID = ?");
                            PreparedStatement playersDelete = connection.prepareStatement("DELETE FROM LOCK_PLAYERS WHERE LOCK_ID = ?");
                            PreparedStatement lockDelete = connection.prepareStatement("DELETE FROM LOCK WHERE ID = ?");
                    ) {
                        locationDelete.setLong(1, lockKey.get());
                        locationDelete.execute();

                        playersDelete.setLong(1, lockKey.get());
                        playersDelete.execute();

                        lockDelete.setLong(1, lockKey.get());
                        lockDelete.execute();
                    }
                } else {
                    try (
                            PreparedStatement locationDelete = connection.prepareStatement(
                                    "DELETE FROM LOCK_LOCATIONS WHERE WORLD_UUID = ? AND X = ? AND Y = ? AND Z = ?");
                    ) {
                        locationDelete.setObject(1, location.getExtent().getUniqueId());
                        locationDelete.setInt(2, location.getBlockX());
                        locationDelete.setInt(3, location.getBlockY());
                        locationDelete.setInt(4, location.getBlockZ());
                        locationDelete.execute();
                    }
                }

                connection.close();
            } catch (SQLException e) {
                    getLogger().error("Error deleteLock for location: " + location.toString());
                    e.printStackTrace();
            }
        } else {
            getLogger().error("Error deleteLock for location (ID not found): " + location.toString());
        }
    }

    public void deleteLocksForPlayer(UUID player) {
        try (
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT ID FROM LOCK WHERE OWNER_UUID = ?");
        ) {

            ps.setString(1, player.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try (
                        PreparedStatement psLocations = connection.prepareStatement("DELETE FROM LOCK_LOCATIONS WHERE LOCK_ID = ?");
                        PreparedStatement psAccessors = connection.prepareStatement("DELETE FROM LOCK_PLAYERS WHERE LOCK_ID = ?");
                        PreparedStatement psLocks = connection.prepareStatement("DELETE FROM LOCK WHERE ID = ?");
                    ) {
                        String lockId = rs.getString(1);

                        psLocations.setString(1, lockId);
                        psAccessors.setString(1, lockId);
                        psLocks.setString(1, lockId);

                        psLocations.execute();
                        psAccessors.execute();
                        psLocks.execute();
                    }
                }
            }

        } catch (SQLException e) {
            getLogger().error("Error deleting locks for player: " + player.toString());
        }

    }

    public boolean isUniqueName(UUID playerUUID, String lockName) {
        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM LOCK WHERE OWNER_UUID = ? AND LOCK_NAME = ?");
        ) {
            ps.setObject(1, playerUUID);
            ps.setString(2, lockName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("COUNT(*)") == 0;
                }
            }

        } catch (SQLException e) {
            getLogger().error("Error isUniqueName: " + playerUUID.toString() + ", for lock: " + lockName);
            e.printStackTrace();
        }

        return false;
    }

    public String getRandomLockName(UUID owner, String lockedObjectName) {
        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement("SELECT LOCK_NAME FROM LOCK WHERE LOCK_NAME LIKE ?");
        ) {
            ps.setString(1, lockedObjectName + "%");

            try (ResultSet rs = ps.executeQuery()) {
                HashSet<String> usedNames = new HashSet<>();

                while (rs.next()) {
                    usedNames.add(rs.getString("LOCK_NAME"));
                }

                for (int i = 0; i <= usedNames.size(); i++) {
                    if (!usedNames.contains(lockedObjectName + i)) {
                        //Modify lockecObjectName - limit it to the 25character max of names
                        return lockedObjectName.substring(0, Math.min(25 - String.valueOf(i).length(), lockedObjectName.length())) + i;
                    }
                }
            }

        } catch (SQLException e) {
            getLogger().error("Error getRandomLockName: " + owner + " for " + lockedObjectName);
            e.printStackTrace();
        }

        //If above fails return random name, hope it's not taken
        int randomInt = (int) (10 + Math.random() * 100);
        return lockedObjectName.substring(0, Math.min(25 - String.valueOf(randomInt).length(), lockedObjectName.length())) + randomInt;
    }

    public void addLockLocation(Lock lock, Location<World> location) {
        Optional<Integer> id = getLockID(lock);

        if (id.isPresent()) {
            try (
                    Connection connection = getConnection();
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO LOCK_LOCATIONS(LOCK_ID, WORLD_UUID, X, Y, Z) VALUES (?, ?, ?, ?, ?)");
            ) {
                ps.setInt(1, id.get());
                ps.setObject(2, location.getExtent().getUniqueId());
                ps.setInt(3, location.getBlockX());
                ps.setInt(4, location.getBlockY());
                ps.setInt(5, location.getBlockZ());
                ps.executeUpdate();
            } catch (SQLException e) {
                getLogger().error("Error addLockLocation for " + lock.getName() + ", owner: " + lock.getOwner() + ", location: " + location.toString());
                e.printStackTrace();
            }
        }
    }

    public void removeLockAccess(Lock thisLock, UUID uniqueId) {
        Optional<Integer> id = getLockID(thisLock);

        if (id.isPresent()) {
            try (
                    Connection connection = getConnection();
                    PreparedStatement ps = connection.prepareStatement("DELETE FROM LOCK_PLAYERS WHERE LOCK_ID = ? AND PLAYER_UUID = ?");
            ) {
                ps.setInt(1, id.get());
                ps.setObject(2, uniqueId);
                ps.executeUpdate();
            } catch (SQLException e) {
                getLogger().error("Error removeLockAccess for " + thisLock.getName() + ", owner: " + thisLock.getOwner() + ", player: " + uniqueId);
                e.printStackTrace();
            }
        }
    }

    public void updateLockAttributes(UUID originalOwner, String originalName, Lock thisLock) {
        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "UPDATE LOCK SET OWNER_UUID = ?, LOCK_NAME = ?, LOCK_TYPE = ?, PASSWORD = ?, SALT = ?, REDSTONE_PROTECT = ?, ACCESSED = ? WHERE OWNER_UUID = ? AND LOCK_NAME = ?");
        ) {
            ps.setString(1, thisLock.getOwner().toString());
            ps.setString(2, thisLock.getName());
            ps.setString(3, thisLock.getLockType().toString());
            ps.setString(4, thisLock.getPassword());
            ps.setBytes(5, thisLock.getSalt());
            ps.setBoolean(6, thisLock.getProtectFromRedstone());
            ps.setTimestamp(7, Timestamp.valueOf(thisLock.getLastAccessed()));
            ps.setString(8, originalOwner.toString());
            ps.setString(9, originalName);

            ps.executeUpdate();
        } catch (SQLException e) {
            getLogger().error("Error updateLockAttributes for future lock " + thisLock.getName() + ", owner: " + thisLock.getOwner());
            e.printStackTrace();
        }
    }

    public void removeAllLockAccess(Lock thisLock) {
        Optional<Integer> id = getLockID(thisLock);

        if (id.isPresent()) {
            try (
                    Connection connection = getConnection();
                    PreparedStatement ps = connection.prepareStatement("DELETE FROM LOCK_PLAYERS WHERE LOCK_ID = ?");
            ) {
                ps.setInt(1, id.get());
                ps.executeUpdate();
            } catch (SQLException e) {
                getLogger().error("Error removeAllLockAccess for " + thisLock.getName() + ", owner: " + thisLock.getOwner());
                e.printStackTrace();
            }
        }
    }

    public List<Lock> getLocksByOwner(UUID uniqueId) {
        List<Lock> locks = new ArrayList<>();

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT ID, OWNER_UUID, LOCK_NAME, LOCK_TYPE, LOCKED_OBJECT, SALT, PASSWORD, REDSTONE_PROTECT, ACCESSED FROM LOCK WHERE OWNER_UUID = ?");
        ) {
            ps.setString(1, uniqueId.toString());

            try (
                    ResultSet rs = ps.executeQuery();
            ) {
                while (rs.next()) {
                    locks.add(new Lock(
                            UUID.fromString(rs.getString("OWNER_UUID")),
                            rs.getString("LOCK_NAME"),
                            LockType.valueOf(rs.getString("LOCK_TYPE")),
                            getLockLocationsByID(rs.getInt("ID")),
                            rs.getString("LOCKED_OBJECT"),
                            rs.getBytes("SALT"),
                            rs.getString("PASSWORD"),
                            getAbleToAccessByID(rs.getInt("ID")),
                            rs.getBoolean("REDSTONE_PROTECT"),
                            rs.getTimestamp("ACCESSED").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            getLogger().error("Error getLocksByOwner for owner: " + uniqueId);
            e.printStackTrace();
        }

        return locks;
    }

    public boolean isPlayerAtLockLimit(UUID player, LockType type, HashMap<String, Integer> limits) {
        //If a maximum isn't defined, no limit
        if (!limits.containsKey("total") && !limits.containsKey(type.toString().toLowerCase())) {
            return false;
        }

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT COUNT(ID) as TOTAL, SUM(CASE WHEN LOCK.LOCK_TYPE = ? THEN 1 ELSE 0 END) AS TYPE_TOTAL FROM LOCK WHERE LOCK.OWNER_UUID = ?");
        ) {
            ps.setString(1, type.toString());
            ps.setString(2, player.toString());

            try (ResultSet rs = ps.executeQuery()) {
                //If total limit set and query says we're above that.. or if type limit is set and query says we're above that
                return rs.next() && ( //if !rs.next(), no locks detected
                        (limits.containsKey("total") && rs.getInt("TOTAL") >= limits.get("total")) || (
                                limits.containsKey(type.toString().toLowerCase())
                                        && rs.getInt("TYPE_TOTAL") >= limits.get(type.toString().toLowerCase())));
            }

        } catch (SQLException e) {
            getLogger().error("Error isPlayerAtLockLimit: " + player + ", " + type);
            e.printStackTrace();
        }

        //Sql exception, prevent placement
        return true;
    }

    public void updateLastAccessed(UUID player, LocalDateTime now) {
        try (PreparedStatement ps = getConnection().prepareStatement("UPDATE LOCK SET ACCESSED = ? WHERE OWNER_UUID = ?")) {
            ps.setTimestamp(1, Timestamp.valueOf(now));
            ps.setString(2, player.toString());
        } catch (SQLException e) {
            getLogger().error("Error updateLastAccessed: " + player);
            e.printStackTrace();
        }
    }

}
