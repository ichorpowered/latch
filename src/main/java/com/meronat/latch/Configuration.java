/*
 * This file is part of Latch, licensed under the MIT License.
 *
 * Copyright (c) 2016-2018 IchorPowered <https://github.com/IchorPowered>
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

package com.meronat.latch;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.meronat.latch.enums.LockType;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {

    private CommentedConfigurationNode rootNode;
    private final ConfigurationLoader<CommentedConfigurationNode> configManager;

    public Configuration(ConfigurationLoader<CommentedConfigurationNode> configManager) {
        this.configManager = configManager;
        try {
            this.rootNode = configManager.load();
        } catch (IOException e) {
            Latch.getLogger().error("Unable to load configuration, starting with a default one.");
            this.rootNode = configManager.createEmptyNode();
        }

        loadDefaults();
        saveConfig();
    }

    private void loadDefaults() {
        //Should we add latch.normal to default permissions?
        if (this.rootNode.getNode("add_default_permissions").isVirtual()) {
            this.rootNode.getNode("add_default_permissions").setValue(false);
        }

        //Blocks we're able to lock
        if (this.rootNode.getNode("lockable_blocks").isVirtual()) {
            final List<String> lockableBlocks = new ArrayList<>();

            lockableBlocks.add(BlockTypes.CHEST.getId());
            lockableBlocks.add(BlockTypes.TRAPPED_CHEST.getId());

            lockableBlocks.add(BlockTypes.BLACK_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.BLUE_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.BROWN_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.CYAN_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.GRAY_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.GREEN_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.LIGHT_BLUE_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.LIME_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.MAGENTA_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.ORANGE_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.PINK_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.PURPLE_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.RED_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.SILVER_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.WHITE_SHULKER_BOX.getId());
            lockableBlocks.add(BlockTypes.YELLOW_SHULKER_BOX.getId());

            lockableBlocks.add(BlockTypes.BREWING_STAND.getId());
            lockableBlocks.add(BlockTypes.JUKEBOX.getId());
            lockableBlocks.add(BlockTypes.FURNACE.getId());
            lockableBlocks.add(BlockTypes.LIT_FURNACE.getId());

            lockableBlocks.add(BlockTypes.HOPPER.getId());
            lockableBlocks.add(BlockTypes.DISPENSER.getId());
            lockableBlocks.add(BlockTypes.DROPPER.getId());

            // Fix issue with doors, possibly from MalisisDoors, look into it more later and revisit in 1.13
            Sponge.getRegistry().getType(BlockType.class, "minecraft:fence_gate").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:spruce_fence_gate").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:birch_fence_gate").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:jungle_fence_gate").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:dark_oak_fence_gate").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:acacia_fence_gate").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:wooden_door").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:spruce_door").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:birch_door").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:jungle_door").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:acacia_door").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:dark_oak_door").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:iron_door").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:trapdoor").ifPresent(t -> lockableBlocks.add(t.getId()));
            Sponge.getRegistry().getType(BlockType.class, "minecraft:iron_trapdoor").ifPresent(t -> lockableBlocks.add(t.getId()));

            this.rootNode.getNode("lockable_blocks").setValue(lockableBlocks);
        }

        //Should we protect locks from explosions?
        if (this.rootNode.getNode("protect_from_explosives").isVirtual()) {
            this.rootNode.getNode("protect_from_explosives").setValue(true);
        }

        //Blocks we should prevent being placed next to locks the player doesn't own
        if (this.rootNode.getNode("prevent_adjacent_to_locks").isVirtual()) {
            final List<String> preventAdjacent = new ArrayList<>();
            preventAdjacent.add(BlockTypes.HOPPER.getId());
            this.rootNode.getNode("prevent_adjacent_to_locks").setValue(preventAdjacent);
        }

        //Blocks that rely on a block under them to stay intact
        if (this.rootNode.getNode("protect_below_block").isVirtual()) {
            final List<String> protectBelowBlock = new ArrayList<>();
            protectBelowBlock.add(BlockTypes.ACACIA_DOOR.getId());
            protectBelowBlock.add(BlockTypes.BIRCH_DOOR.getId());
            protectBelowBlock.add(BlockTypes.DARK_OAK_DOOR.getId());
            protectBelowBlock.add(BlockTypes.WOODEN_DOOR.getId());
            protectBelowBlock.add(BlockTypes.JUNGLE_DOOR.getId());
            protectBelowBlock.add(BlockTypes.SPRUCE_DOOR.getId());

            this.rootNode.getNode("protect_below_block").setValue(protectBelowBlock);
        }

        //Lock limit per enum
        if (this.rootNode.getNode("lock_limit").isVirtual()) {
            final HashMap<String, Integer> limits = new HashMap<>();
            limits.put("total", 64);

            for (LockType type : LockType.values()) {
                limits.put(type.toString().toLowerCase(), 12);
            }
            limits.put(LockType.PRIVATE.toString().toLowerCase(), 24);

            try {
                this.rootNode.getNode("lock_limit").setValue(new TypeToken<Map<String, Integer>>() {}, limits);
            } catch (ObjectMappingException e) {
                this.rootNode.getNode("lock_limit").setValue(limits);
                e.printStackTrace();
            }
        }

        //Do we allow redstone protection?
        if (this.rootNode.getNode("protect_from_redstone").isVirtual()) {
            this.rootNode.getNode("protect_from_redstone").setValue(false);
        }

        if (this.rootNode.getNode("auto_lock_on_placement").isVirtual()) {
            this.rootNode.getNode("auto_lock_on_placement").setValue(false);
        }

        if (this.rootNode.getNode("remove_bypass_on_logout").isVirtual()) {
            this.rootNode.getNode("remove_bypass_on_logout").setValue(true);
        }

        if (this.rootNode.getNode("clean_old_locks").isVirtual()) {
            this.rootNode.getNode("clean_old_locks").setValue(false);
        }

        if (this.rootNode.getNode("clean_old_locks_interval").isVirtual()) {
            this.rootNode.getNode("clean_old_locks_interval").setValue(4);
        }

        if (this.rootNode.getNode("clean_locks_older_than").isVirtual()) {
            this.rootNode.getNode("clean_locks_older_than").setValue(40);
        }

        if (this.rootNode.getNode("allow_opening_locked_iron").isVirtual()) {
            this.rootNode.getNode("allow_opening_locked_iron").setComment("Allows opening locked iron doors and trapdoors by right clicking.");
            this.rootNode.getNode("allow_opening_locked_iron").setValue(true);
        }

    }

    public boolean allowOpeningLockedIron() {
        return this.rootNode.getNode("allow_opening_locked_iron").getBoolean(false);
    }

    /*
    public boolean addLockableBlock(BlockType blockType) {
        final CommentedConfigurationNode node = this.rootNode.getNode("lockable_blocks");
        try {
            node.setValue(node.getList(TypeToken.of(String.class)).add(blockType.getId()));
            saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean removeLockableBlock(BlockType blockType) {
        final CommentedConfigurationNode node = this.rootNode.getNode("lockable_blocks");
        try {
            node.setValue(node.getList(TypeToken.of(String.class)).remove(blockType.getId()));
            saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    */

    private void saveConfig() {
        try {
            this.configManager.save(this.rootNode);
        } catch (IOException e) {
            Latch.getLogger().error("There were issues saving the configuration.");
        }
    }

    public void reloadConfig() {
        try {
            this.rootNode = configManager.load();
        } catch (IOException e) {
            Latch.getLogger().error("Unable to load configuration, starting with a default one.");
            this.rootNode = configManager.createEmptyNode();
        }

        this.loadDefaults();
        this.saveConfig();
    }

    public CommentedConfigurationNode getRootNode() {
        return this.rootNode;
    }

    public boolean setLockableBlocks(ImmutableSet<String> blockTypes) {
        final CommentedConfigurationNode node = this.rootNode.getNode("lockable_blocks");
        try {
            node.setValue(blockTypes);
            saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
