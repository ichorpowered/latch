package com.ichorcommunity.latch.storage;

import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import com.ichorcommunity.latch.enums.LockType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ichorcommunity.latch.Latch.getLogger;

public class SqlHandler {

    private SqlService sql;

    public SqlHandler() {
        try {
            createTables();
        } catch (SQLException e) {
            getLogger().error("Error creating tables in SqlHandler.");
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        if (sql == null) {
            sql = Sponge.getServiceManager().provide(SqlService.class).get();
        }
        return sql.getDataSource("jdbc:h2:" + Latch.getConfigPatch().getParent().toAbsolutePath().toString() + File.separator + "latch").getConnection();
    }

    private void createTables() throws SQLException {
        Connection connection = getConnection();

        String createLockTable = "CREATE TABLE IF NOT EXISTS LOCK (" +
                "ID bigint AUTO_INCREMENT, " +
                "OWNER_UUID char(36) NOT NULL, " +
                "LOCK_NAME varchar(25) NOT NULL, " +
                "LOCK_TYPE varchar(15) NOT NULL, " +
                "LOCKED_OBJECT varchar(50) NOT NULL, " +
                "PASSWORD varchar(256) NOT NULL, " +
                "SALT varchar(64) NOT NULL, " +
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

        connection.prepareStatement(createLockTable).execute();
        connection.prepareStatement(createLocationTable).execute();
        connection.prepareStatement(createPlayerTable).execute();
        connection.close();
    }

    public Optional<Lock> getLockByLocation(Location location) {
        Optional<Lock> lock = Optional.empty();

        String getLockByLocation = "SELECT ID, OWNER_UUID, LOCK_NAME, LOCK_TYPE, LOCKED_OBJECT, SALT, PASSWORD FROM LOCK_LOCATIONS JOIN LOCK ON (LOCK_LOCATIONS.LOCK_ID = LOCK.ID) " +
                "WHERE LOCK_LOCATIONS.WORLD_UUID = ? AND LOCK_LOCATIONS.X = ? AND LOCK_LOCATIONS.Y = ? AND LOCK_LOCATIONS.Z = ?";

        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(getLockByLocation);
            ps.setObject(1, location.getExtent().getUniqueId());
            ps.setInt(2, location.getBlockX());
            ps.setInt(3, location.getBlockY());
            ps.setInt(4, location.getBlockZ());

            ResultSet tempLock = ps.executeQuery();

            //Should only be one lock at this location, if any
            if(tempLock.next()) {
                lock = Optional.of(new Lock(
                        UUID.fromString(tempLock.getString("OWNER_UUID")),
                        tempLock.getString("LOCK_NAME"),
                        LockType.valueOf(tempLock.getString("LOCK_TYPE")),
                        getLockLocationsByID(tempLock.getInt("ID")),
                        tempLock.getString("LOCKED_OBJECT"),
                        tempLock.getBytes("SALT"),
                        tempLock.getString("PASSWORD"),
                        getAbleToAccessByID(tempLock.getInt("ID"))));
            }

            ps.close();
            tempLock.close();
            connection.close();

        } catch (SQLException e) {
            getLogger().error("Error running SQL getLockByLocation: ");
            e.printStackTrace();
        }
        return lock;
    }

    private HashSet<Location<World>> getLockLocationsByID(int id) throws SQLException {
        HashSet<Location<World>> locations = new HashSet<Location<World>>();
        Connection connection = getConnection();

        String getLockLocationsByID = "SELECT WORLD_UUID, X, Y, Z FROM LOCK_LOCATIONS WHERE LOCK_LOCATIONS.LOCK_ID = ?";

        PreparedStatement ps = connection.prepareStatement(getLockLocationsByID);

        ps.setObject(1, id);
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {
            Optional<World> world = Sponge.getServer().getWorld(UUID.fromString(rs.getString("WORLD_UUID")));
            if(world.isPresent()) {
                locations.add(new Location(world.get(), rs.getInt("X"), rs.getInt("Y"), rs.getInt("Z")));
            } else {
                getLogger().error("Error loading location in getLockLocationsByID: " + rs.getString("WORLD_UUID") + " does not exist as a world (ID: " + id + ")");
            }
        }

        ps.close();
        rs.close();
        connection.close();

        return locations;
    }

    private HashSet<UUID> getAbleToAccessByID(int id) throws SQLException {
        HashSet<UUID> players = new HashSet<UUID>();
        Connection connection = getConnection();

        String getAllowedPlayersByID = "SELECT PLAYER_UUID FROM LOCK_PLAYERS WHERE LOCK_PLAYERS.LOCK_ID = ?";

        PreparedStatement ps = connection.prepareStatement(getAllowedPlayersByID);

        ps.setObject(1, id);
        ResultSet rs = ps.executeQuery();

        while(rs.next()) {
            players.add(UUID.fromString(rs.getString("PLAYER_UUID")));
        }

        ps.close();
        rs.close();
        connection.close();

        return players;
    }

    public Optional<Integer> getLockID(Lock lock) {
        return getLockID(lock.getOwner(), lock.getName());
    }

    private Optional<Integer> getLockID(UUID owner, String name) {
        Optional<Integer> id = Optional.empty();
        try {
            Connection connection = getConnection();

            String getLockIDByName = "SELECT ID FROM LOCK WHERE LOCK.OWNER_UUID = ? AND LOCK.LOCK_NAME = ?";

            PreparedStatement ps = connection.prepareStatement(getLockIDByName);

            ps.setObject(1, owner);
            ps.setString(2, name);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                id = Optional.of(rs.getInt("ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    private Optional<Integer> getLockID(Location location) throws SQLException {
        Optional<Integer> id = Optional.empty();
        Connection connection = getConnection();

        String getLockIDByLocation = "SELECT LOCK_ID FROM LOCK_LOCATIONS WHERE LOCK_LOCATIONS.WORLD_UUID = ? AND LOCK_LOCATIONS.X = ? AND LOCK_LOCATIONS.Y = ? AND LOCK_LOCATIONS.Z = ?";

        PreparedStatement ps = connection.prepareStatement(getLockIDByLocation);
        ps.setObject(1, location.getExtent().getUniqueId());
        ps.setInt(2, location.getBlockX());
        ps.setInt(3, location.getBlockY());
        ps.setInt(4, location.getBlockZ());

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            id = Optional.of(rs.getInt("LOCK_ID"));
        }
        return id;
    }

    public void addLockAccess(Lock thisLock, UUID player) {

        String addPlayerAccess = "INSERT INTO LOCK_PLAYERS(LOCK_ID, PLAYER_UUID) VALUES ?, ?";

        try {
            Optional<Integer> id = getLockID(thisLock);

            if(id.isPresent()) {
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(addPlayerAccess);

                ps.setInt(1, id.get());
                ps.setObject(2, player);
                ps.executeUpdate();

                ps.close();
                connection.close();
            }
        } catch (SQLException e) {
            getLogger().error("Error addLockAccess for " + thisLock.getName() + ", owner: " + thisLock.getOwner() + ", player: " + player);
            e.printStackTrace();
        }

    }

    public void createLock(Lock lock, HashSet<Location<World>> locations, HashSet<UUID> ableToAccess) {
        String createLock = "INSERT INTO LOCK(OWNER_UUID, LOCK_NAME, LOCK_TYPE, LOCKED_OBJECT, SALT, PASSWORD) VALUES " +
                "(?, ?, ?, ?, ?, ?)";

        String createLockPlayers = "INSERT INTO LOCK_PLAYERS(LOCK_ID, PLAYER_UUID) VALUES (?, ?)";

        String createLockLocations = "INSERT INTO LOCK_LOCATIONS(LOCK_ID, WORLD_UUID, X, Y, Z) VALUES (?, ?, ?, ?, ?)";

        try {
            Connection connection = getConnection();
            long lockKey = -1;

            PreparedStatement psLock = connection.prepareStatement(createLock, PreparedStatement.RETURN_GENERATED_KEYS);
            psLock.setObject(1, lock.getOwner());
            psLock.setString(2, lock.getName());
            psLock.setString(3, lock.getLockType().toString());
            psLock.setString(4, lock.getLockedObject());
            psLock.setBytes(5, lock.getSalt());
            psLock.setString(6, lock.getPassword());

            psLock.executeUpdate();
            ResultSet rsLock = psLock.getGeneratedKeys();

            if(rsLock.next()) {
                lockKey = rsLock.getLong(1);
            } else {
                throw new SQLException("ResultSet did not return a PK for subsequent inserts.");
            }

            rsLock.close();
            psLock.close();

            PreparedStatement psLocations = connection.prepareStatement(createLockLocations);
            //Need to ignore existing locations
            for(Location<World> location : locations) {
                psLocations.setLong(1, lockKey);
                psLocations.setObject(2, location.getExtent().getUniqueId());
                psLocations.setInt(3, location.getBlockX());
                psLocations.setInt(4, location.getBlockY());
                psLocations.setInt(5, location.getBlockZ());
                psLocations.addBatch();
            }
            psLocations.executeBatch();
            psLocations.close();

            PreparedStatement psPlayers = connection.prepareStatement(createLockPlayers);
            for(UUID uuid : ableToAccess) {
                psPlayers.setLong(1, lockKey);
                psPlayers.setObject(2, uuid);
                psPlayers.addBatch();
            }
            psPlayers.executeBatch();
            psPlayers.close();

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void deleteLock(Location<World> location, boolean deleteEntireLock) {
        try {
            Optional<Integer> lockKey = getLockID(location);
            boolean fullyDelete = deleteEntireLock; //do we fully delete the lock (from all tables)

            if(lockKey.isPresent()) {
                Connection connection = getConnection();

                //If not forced to delete the entire lock(all locations), only do it if it's the last location
                if(!fullyDelete) {
                    PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM LOCK_LOCATIONS WHERE LOCK_ID = ?");
                    ps.setLong(1, lockKey.get());
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        fullyDelete = rs.getLong(1) <= 1;//if only one row left, fully delete
                    } else {
                        fullyDelete = true; //or if no locations left, fully delete
                    }
                }

                if(fullyDelete) {
                    PreparedStatement locationDelete = connection.prepareStatement("DELETE FROM LOCK_LOCATIONS WHERE LOCK_ID = ?");
                    locationDelete.setLong(1, lockKey.get());
                    locationDelete.execute();
                    locationDelete.close();

                    PreparedStatement playersDelete = connection.prepareStatement("DELETE FROM LOCK_PLAYERS WHERE LOCK_ID = ?");
                    playersDelete.setLong(1, lockKey.get());
                    playersDelete.execute();
                    playersDelete.close();

                    PreparedStatement lockDelete = connection.prepareStatement("DELETE FROM LOCK WHERE ID = ?");
                    lockDelete.setLong(1, lockKey.get());
                    lockDelete.execute();
                    lockDelete.close();
                } else {
                    PreparedStatement locationDelete = connection.prepareStatement("DELETE FROM LOCK_LOCATIONS WHERE WORLD_UUID = ? AND X = ? AND Y = ? AND Z = ?");
                    locationDelete.setObject(1, location.getExtent().getUniqueId());
                    locationDelete.setInt(2, location.getBlockX());
                    locationDelete.setInt(3, location.getBlockY());
                    locationDelete.setInt(4, location.getBlockZ());
                    locationDelete.execute();
                    locationDelete.close();
                }

                connection.close();
            } else {
                getLogger().error("Error deleteLock for location (ID not found): " + location.toString());
            }
        } catch (SQLException e) {
            getLogger().error("Error deleteLock for location: " + location.toString());
            e.printStackTrace();
        }
    }

    public boolean isUniqueName(UUID playerUUID, String lockName) {
        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM LOCK WHERE OWNER_UUID = ? AND LOCK_NAME = ?");
            ps.setObject(1, playerUUID);
            ps.setString(2, lockName);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                return rs.getLong("COUNT(*)") == 0;
            }

            rs.close();
            ps.close();
            connection.close();

        } catch (SQLException e) {
            getLogger().error("Error isUniqueName: " + playerUUID.toString() + ", for lock: " + lockName);
            e.printStackTrace();
        }

        return false;
    }

    public String getRandomLockName(UUID owner, String lockedObjectName) {
        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT LOCK_NAME FROM LOCK WHERE LOCK_NAME LIKE ?");
            ps.setString(1, lockedObjectName + "%");

            ResultSet rs = ps.executeQuery();

            HashSet<String> usedNames = new HashSet<String>();

            while(rs.next()) {
                usedNames.add(rs.getString("LOCK_NAME"));
            }

            for(int i = 0; i <= usedNames.size(); i++) {
                if( !usedNames.contains(lockedObjectName+i)) {
                    //Modify lockecObjectName - limit it to the 25character max of names
                    return lockedObjectName.substring(0, Math.min(25 - String.valueOf(i).length(), lockedObjectName.length()))+i;
                }
            }
            rs.close();
            ps.close();
            connection.close();
        } catch (SQLException e) {
            getLogger().error("Error getRandomLockName: " + owner + " for " + lockedObjectName);
            e.printStackTrace();
        }

        //If above fails return random name, hope it's not taken
        int randomInt = (int) (10 + Math.random()*100);
        return lockedObjectName.substring(0, Math.min(25 - String.valueOf(randomInt).length(), lockedObjectName.length()))+randomInt ;
    }

    public void addLockLocation(Lock lock, Location<World> location) {
        String createLockLocations = "INSERT INTO LOCK_LOCATIONS(LOCK_ID, WORLD_UUID, X, Y, Z) VALUES (?, ?, ?, ?, ?)";

        try {
            Optional<Integer> id = getLockID(lock);

            if(id.isPresent()) {
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(createLockLocations);

                ps.setInt(1, id.get());
                ps.setObject(2, location.getExtent().getUniqueId());
                ps.setInt(3, location.getBlockX());
                ps.setInt(4, location.getBlockY());
                ps.setInt(5, location.getBlockZ());
                ps.executeUpdate();

                ps.close();
                connection.close();
            }
        } catch (SQLException e) {
            getLogger().error("Error addLockLocation for " + lock.getName() + ", owner: " + lock.getOwner() + ", location: " + location.toString());
            e.printStackTrace();
        }

    }

    public void removeLockAccess(Lock thisLock, UUID uniqueId) {
        String removePlayerAccess = "DELETE FROM LOCK_PLAYERS WHERE LOCK_ID = ? AND PLAYER_UUID = ?";

        try {
            Optional<Integer> id = getLockID(thisLock);

            if(id.isPresent()) {
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(removePlayerAccess);

                ps.setInt(1, id.get());
                ps.setObject(2, uniqueId);
                ps.executeUpdate();

                ps.close();
                connection.close();
            }
        } catch (SQLException e) {
            getLogger().error("Error removeLockAccess for " + thisLock.getName() + ", owner: " + thisLock.getOwner() + ", player: " + uniqueId);
            e.printStackTrace();
        }
    }

    public void updateLockAttributes(UUID originalOwner, String originalName, Lock thisLock) {
        String updateLockAttributes = "UPDATE LOCK SET OWNER_UUID = ?, LOCK_NAME = ?, LOCK_TYPE = ?, PASSWORD = ?, SALT = ? WHERE OWNER_UUID = ? AND LOCK_NAME = ?";

        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(updateLockAttributes);

            ps.setString(1, thisLock.getOwner().toString());
            ps.setString(2, thisLock.getName());
            ps.setString(3, thisLock.getLockType().toString());
            ps.setString(4, thisLock.getPassword());
            ps.setBytes(5, thisLock.getSalt());
            ps.setString(6, originalOwner.toString());
            ps.setString(7, originalName);

            ps.executeUpdate();

            ps.close();
            connection.close();
        } catch (SQLException e) {
            getLogger().error("Error updateLockAttributes for future lock " + thisLock.getName() + ", owner: " + thisLock.getOwner());
            e.printStackTrace();
        }
    }

    public void removeAllLockAccess(Lock thisLock) {
        String removePlayerAccess = "DELETE FROM LOCK_PLAYERS WHERE LOCK_ID = ?";

        try {
            Optional<Integer> id = getLockID(thisLock);

            if(id.isPresent()) {
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(removePlayerAccess);

                ps.setInt(1, id.get());
                ps.executeUpdate();

                ps.close();
                connection.close();
            }
        } catch (SQLException e) {
            getLogger().error("Error removeAllLockAccess for " + thisLock.getName() + ", owner: " + thisLock.getOwner());
            e.printStackTrace();
        }
    }

    public List<Lock> getLocksByOwner(UUID uniqueId) {
        List<Lock> locks = new ArrayList<Lock>();

        String getLockByOwner = "SELECT ID, OWNER_UUID, LOCK_NAME, LOCK_TYPE, LOCKED_OBJECT, SALT, PASSWORD FROM LOCK WHERE OWNER_UUID = ?";

        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(getLockByOwner);
            ps.setString(1, uniqueId.toString());

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                locks.add(new Lock(
                        UUID.fromString(rs.getString("OWNER_UUID")),
                        rs.getString("LOCK_NAME"),
                        LockType.valueOf(rs.getString("LOCK_TYPE")),
                        getLockLocationsByID(rs.getInt("ID")),
                        rs.getString("LOCKED_OBJECT"),
                        rs.getBytes("SALT"),
                        rs.getString("PASSWORD"),
                        getAbleToAccessByID(rs.getInt("ID"))));
            }
        } catch (SQLException e) {
            getLogger().error("Error getLocksByOwner for owner: " + uniqueId);
            e.printStackTrace();
        }

        return locks;
    }

    public boolean isPlayerAtLockLimit(UUID player, LockType type, HashMap<String, Integer> limits) {
        String getLockTotals = "SELECT COUNT(ID) as TOTAL, SUM(CASE WHEN LOCK.LOCK_TYPE = ? THEN 1 ELSE 0 END) AS TYPE_TOTAL FROM LOCK WHERE LOCK.OWNER_UUID = ?";

        //If a maximum isn't defined, no limit
        if(!limits.containsKey("total") && !limits.containsKey(type.toString().toLowerCase())) {
            return false;
        }

        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(getLockTotals);
            ps.setString(1, type.toString());
            ps.setString(2, player.toString());

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                //If total limit set and query says we're above that.. or if type limit is set and query says we're above that
                return ( limits.containsKey("total") && rs.getInt("TOTAL") >= limits.get("total")) ||
                        (limits.containsKey(type.toString().toLowerCase()) && rs.getInt("TYPE_TOTAL") >= limits.get(type.toString().toLowerCase()));

            } else {
                //no locks detected
                return false;
            }
        } catch (SQLException e) {
            getLogger().error("Error isPlayerAtLockLimit: " + player + ", " + type);
            e.printStackTrace();
        }

        //Sql exception, prevent placement
        return true;
    }
}
