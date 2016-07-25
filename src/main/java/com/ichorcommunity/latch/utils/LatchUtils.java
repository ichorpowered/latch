package com.ichorcommunity.latch.utils;

import com.google.common.collect.ImmutableList;
import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ichorcommunity.latch.Latch.getLogger;

public class LatchUtils {

    private static final int ITERATIONS = 1000;
    private static final int KEY_LENGTH = 256;

    private static final ImmutableList<Direction> adjacentDirections =
            ImmutableList.of(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    public static String getBlockNameFromType(BlockType type) {
        if(type.getName().lastIndexOf(':')+1 <= type.getName().length()) {
            return type.getName().substring(type.getName().lastIndexOf(':') + 1);
        }
        return type.getName();
    }

    public static List<Lock> getAdjacentLocks(Location location) {
        List<Lock> lockList = new ArrayList<Lock>();

        for( Direction d : adjacentDirections) {
            if(Latch.getLockManager().isLockableBlock(location.getBlockRelative(d).getBlock().getType())) {
                Optional<Lock> lock = Latch.getLockManager().getLock(location.getBlockRelative(d));
                if(lock.isPresent()) {
                    lockList.add(lock.get());
                }
            }
        }
        return lockList;
    }

    public static Optional<Location<World>> getDoubleBlockLocation(BlockSnapshot block) {
        if(block != BlockSnapshot.NONE && block.getLocation().isPresent() && block.get(Keys.CONNECTED_DIRECTIONS).isPresent() ) {
            for(Direction direction : block.get(Keys.CONNECTED_DIRECTIONS).get()) {
                if( block.getLocation().get().getBlockRelative(direction).getBlock().getType() == block.getState().getType()) {
                    return Optional.of(block.getLocation().get().getBlockRelative(direction));
                }
            }
        }
        return Optional.empty();
    }

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();

        byte[] salt = new byte[8];
        random.nextBytes(salt);

        return salt;
    }

    public static String hashPassword(String password, byte[] salt) {
        char[] passwordChars = password.toCharArray();

        PBEKeySpec spec = new PBEKeySpec(
                passwordChars,
                salt,
                ITERATIONS,
                KEY_LENGTH
        );
        try {
            SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] hashedPassword = key.generateSecret(spec).getEncoded();
            return new String(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            getLogger().error("Password algorithm not detected. Password will be stored as plaintext.");
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            getLogger().error("Password has an invalid key. Password wil be stored as plaintext.");
            e.printStackTrace();
        }
        return password;
    }

    public static String getRandomLockName(UUID owner, String lockedObjectName) {
        return Latch.getStorageHandler().getRandomLockName(owner, lockedObjectName);
    }

    public static String getLocationString(Location<World> worldLocation) {
        return "("+worldLocation.getBlockX()+","+worldLocation.getBlockY()+","+worldLocation.getBlockZ()+")";
    }
}
