package com.ichorcommunity.latch.utils;

import com.google.common.collect.ImmutableList;
import com.ichorcommunity.latch.Latch;
import com.ichorcommunity.latch.entities.Lock;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ichorcommunity.latch.Latch.getLogger;

public class LatchUtils {

    private static final int ITERATIONS = 2;
    private static final int KEY_LENGTH = 256;

    private static final ImmutableList<Direction> adjacentDirections =
            ImmutableList.of(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    public static List<Lock> getAdjacentLocks(Location location) {
        List<Lock> lockList = new ArrayList<Lock>();

        for( Direction d : adjacentDirections) {
            if(Latch.lockManager.isLockableBlock(location.getBlockRelative(d).getBlock().getType())) {
                Optional<Lock> lock = Latch.lockManager.getLock(location.getBlockRelative(d));
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
                    return Optional.ofNullable(block.getLocation().get().getBlockRelative(direction));
                }
            }
        }
        return Optional.ofNullable(null);
    }

    public static String hashPassword(String password, Location salt) {
        char[] passwordChars = password.toCharArray();
        String locationKey = salt.getExtent().getUniqueId() + "," + salt.getBlockX() + "," + salt.getBlockY() + "," + salt.getBlockZ();
        byte[] saltBytes = locationKey.getBytes();

        PBEKeySpec spec = new PBEKeySpec(
                passwordChars,
                saltBytes,
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

}
