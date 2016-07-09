package com.ichorcommunity.latch;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.block.BlockTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configuration {

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
        List<String> lockableBlocks = new ArrayList<String>();
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

        if(configNode.getNode("lockable_blocks").isVirtual()) {
            configNode.getNode("lockable_blocks").setValue(lockableBlocks);
        }

        if(configNode.getNode("protect_from_explosives").isVirtual()) {
            configNode.getNode("protect_from_explosives").setValue(true);
        }

        List<String> preventAdjacent = new ArrayList<String>();
        preventAdjacent.add(BlockTypes.HOPPER.getId());

        if(configNode.getNode("prevent_adjacent_to_locks").isVirtual()) {
            configNode.getNode("prevent_adjacent_to_locks").setValue(preventAdjacent);
        }
    }

    public void saveConfig() {
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
