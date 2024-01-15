package net.sideways_sky.multimine;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class DamagedBlockDeleteEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();
    private final DamagedBlock damagedBlock;

    public DamagedBlockDeleteEvent(Block block, DamagedBlock damagedBlock) {
        super(block);
        this.block = block;
        this.damagedBlock = damagedBlock;
    }
    public DamagedBlock getDamage() {
        return damagedBlock;
    }
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
