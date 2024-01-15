package net.sideways_sky.multimine;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;

import java.util.HashMap;
import java.util.Map;

public class Events implements Listener {
    static Map<Block, DamagedBlock> damagedBlockMap = new HashMap<>();
    @EventHandler
    public static void onDamagedBlockDelete(DamagedBlockDeleteEvent e){
        damagedBlockMap.remove(e.getBlock());
    }
    @EventHandler
    public static void onBlockDamage(BlockDamageEvent e){
        if(e.getInstaBreak()){return;}
        DamagedBlock damagedBlock;
        if(!damagedBlockMap.containsKey(e.getBlock())){
            damagedBlock = new DamagedBlock(e.getBlock());
            damagedBlockMap.put(e.getBlock(), damagedBlock);
        } else {
            damagedBlock = damagedBlockMap.get(e.getBlock());
        }
        damagedBlock.setActive(e.getPlayer(), true);
    }

    @EventHandler
    public static void onPlayerArmSwing(PlayerArmSwingEvent e){
        Block block = e.getPlayer().getTargetBlockExact(120);
        if(block == null){return;}
        DamagedBlock damagedBlock = damagedBlockMap.get(block);
        if(damagedBlock == null){return;}
        damagedBlock.damageTick(e.getPlayer());
    }

    @EventHandler
    public static void onBlockBreak(BlockBreakEvent e){
        DamagedBlock damagedBlock = damagedBlockMap.get(e.getBlock());
        if(damagedBlock == null){return;}
        damagedBlock.delete(false);
    }

    @EventHandler
    public static void onBlockDamageAbort(BlockDamageAbortEvent e){
        DamagedBlock damagedBlock = damagedBlockMap.get(e.getBlock());
        if(damagedBlock == null){return;}
        damagedBlock.setActive(e.getPlayer(), false);
    }

}
