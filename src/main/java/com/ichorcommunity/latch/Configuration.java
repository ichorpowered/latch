/*
 * This file is part of Latch, licensed under the MIT License (MIT).
 *
 * Copyright (c) Ichor Community <http://www.ichorcommunity.com>
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

package com.ichorcommunity.latch;

import com.ichorcommunity.latch.enums.LockType;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.block.BlockTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Configuration {

    private CommentedConfigurationNode configNode;
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    public Configuration(ConfigurationLoader<CommentedConfigurationNode> configManager) {
        this.configManager = configManager;
        try {
            this.configNode = configManager.load();
        } catch (IOException e) {
            Latch.getLogger().error("Unable to load configuration, starting with a default one.");
            this.configNode = configManager.createEmptyNode();
        }

        loadDefaults();
        saveConfig();
    }

    private void loadDefaults() {

        //Should we add latch.normal to default permissions?
        if(configNode.getNode("add_default_permissions").isVirtual()) {
            configNode.getNode("add_default_permissions").setValue(false);
        }

        //Blocks we're able to lock
        if(configNode.getNode("lockable_blocks").isVirtual()) {
            List<String> lockableBlocks = new ArrayList<>();
            lockableBlocks.add(BlockTypes.CHEST.getId());
            lockableBlocks.add(BlockTypes.TRAPPED_CHEST.getId());

            lockableBlocks.add(BlockTypes.BREWING_STAND.getId());
            lockableBlocks.add(BlockTypes.JUKEBOX.getId());
            lockableBlocks.add(BlockTypes.TRAPDOOR.getId());
            lockableBlocks.add(BlockTypes.FURNACE.getId());
            lockableBlocks.add(BlockTypes.LIT_FURNACE.getId());

            lockableBlocks.add(BlockTypes.HOPPER.getId());
            lockableBlocks.add(BlockTypes.DISPENSER.getId());
            lockableBlocks.add(BlockTypes.DROPPER.getId());

            lockableBlocks.add(BlockTypes.ACACIA_FENCE_GATE.getId());
            lockableBlocks.add(BlockTypes.BIRCH_FENCE_GATE.getId());
            lockableBlocks.add(BlockTypes.DARK_OAK_FENCE_GATE.getId());
            lockableBlocks.add(BlockTypes.FENCE_GATE.getId());
            lockableBlocks.add(BlockTypes.JUNGLE_FENCE_GATE.getId());
            lockableBlocks.add(BlockTypes.SPRUCE_FENCE_GATE.getId());

            lockableBlocks.add(BlockTypes.ACACIA_DOOR.getId());
            lockableBlocks.add(BlockTypes.BIRCH_DOOR.getId());
            lockableBlocks.add(BlockTypes.DARK_OAK_DOOR.getId());
            lockableBlocks.add(BlockTypes.WOODEN_DOOR.getId());
            lockableBlocks.add(BlockTypes.JUNGLE_DOOR.getId());
            lockableBlocks.add(BlockTypes.SPRUCE_DOOR.getId());

            configNode.getNode("lockable_blocks").setValue(lockableBlocks);
        }

        //Should we protect locks from explosions?
        if(configNode.getNode("protect_from_explosives").isVirtual()) {
            configNode.getNode("protect_from_explosives").setValue(true);
        }

        //Blocks we should prevent being placed next to locks the player doesn't own
        if(configNode.getNode("prevent_adjacent_to_locks").isVirtual()) {
            List<String> preventAdjacent = new ArrayList<>();
            preventAdjacent.add(BlockTypes.HOPPER.getId());
            configNode.getNode("prevent_adjacent_to_locks").setValue(preventAdjacent);
        }

        //Blocks that rely on a block under them to stay intact
        if(configNode.getNode("protect_below_block").isVirtual()) {
            List<String> protectBelowBlock = new ArrayList<>();
            protectBelowBlock.add(BlockTypes.ACACIA_DOOR.getId());
            protectBelowBlock.add(BlockTypes.BIRCH_DOOR.getId());
            protectBelowBlock.add(BlockTypes.DARK_OAK_DOOR.getId());
            protectBelowBlock.add(BlockTypes.WOODEN_DOOR.getId());
            protectBelowBlock.add(BlockTypes.JUNGLE_DOOR.getId());
            protectBelowBlock.add(BlockTypes.SPRUCE_DOOR.getId());

            configNode.getNode("protect_below_block").setValue(protectBelowBlock);
        }

        //Lock limit per enum
        if(configNode.getNode("lock_limit").isVirtual()) {
            HashMap<String, Integer> limits = new HashMap<>();
            limits.put("total", 5);
            limits.put(LockType.PUBLIC.toString().toLowerCase(), 2);
            limits.put(LockType.PRIVATE.toString().toLowerCase(), 3);
            limits.put(LockType.PASSWORD_ALWAYS.toString().toLowerCase(), 2);
            limits.put(LockType.PASSWORD_ONCE.toString().toLowerCase(), 2);

            configNode.getNode("lock_limit").setValue(limits);
        }
    }

    private void saveConfig() {
        try {
            configManager.save(configNode);
        } catch (IOException e) {
            Latch.getLogger().error("Issues saving configuration.");
        }
    }

    public CommentedConfigurationNode getConfigurationNode() {
        return configNode;
    }
}
