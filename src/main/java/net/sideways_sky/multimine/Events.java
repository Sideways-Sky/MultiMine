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

import static net.sideways_sky.multimine.MultiMine.debugMessage;

public class Events implements Listener {
    static Map<Block, DamagedBlock> damagedBlockMap = new HashMap<>();
    @EventHandler
    public static void onDamagedBlockDelete(DamagedBlockDeleteEvent e){
        damagedBlockMap.remove(e.getBlock());
    }
    @EventHandler
    public static void onBlockDamage(BlockDamageEvent e){
        if(e.getInstaBreak()){return;}
        if(!damagedBlockMap.containsKey(e.getBlock())){
            damagedBlockMap.put(e.getBlock(), new DamagedBlock(e.getBlock(), e.getPlayer()));
        }else {
            damagedBlockMap.get(e.getBlock()).stopFade();
        }
    }

    @EventHandler
    public static void onPlayerArmSwing(PlayerArmSwingEvent e){
        Block block = e.getPlayer().getTargetBlockExact(120);
        if(block == null){return;}
        DamagedBlock damagedBlock = damagedBlockMap.get(block);
        if(damagedBlock == null){return;}
        damagedBlock.lastPlayer = e.getPlayer();
        damagedBlock.damageTick();
    }

    @EventHandler
    public static void onBlockBreak(BlockBreakEvent e){
        DamagedBlock damagedBlock = damagedBlockMap.get(e.getBlock());
        if(damagedBlock == null){return;}
        debugMessage("Break: " + damagedBlock.damage);
        damagedBlock.lastPlayer = e.getPlayer();
        damagedBlock.delete(false);
    }

    @EventHandler
    public static void onBlockDamageAbort(BlockDamageAbortEvent e){
        DamagedBlock damagedBlock = damagedBlockMap.get(e.getBlock());
        if(damagedBlock == null){return;}
        debugMessage("Abort: " + damagedBlock.damage);
        damagedBlock.startFade();
    }

}
