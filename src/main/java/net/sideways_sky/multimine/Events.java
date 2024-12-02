package net.sideways_sky.multimine;

import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;

import java.util.HashMap;
import java.util.Map;

public class Events implements Listener {
    public static Map<Block, DamagedBlock> damagedBlockMap = new HashMap<>();
    @EventHandler
    public static void onBlockDamage(BlockDamageEvent e){
        if(e.getInstaBreak()){return;}
        DamagedBlock damagedBlock = damagedBlockMap.get(e.getBlock());
        if(damagedBlock == null){
            damagedBlock = new DamagedBlock(e.getBlock());
            damagedBlockMap.put(e.getBlock(), damagedBlock);
        }

        damagedBlock.debugMessage("DamageEvent");
        damagedBlock.entityDamageMap.putIfAbsent(e.getPlayer(), 0F);
        damagedBlock.stopFade();
    }

    @EventHandler
    public static void onBlockBreakProgressUpdate(BlockBreakProgressUpdateEvent e){
        if(e.getProgress() <= 0){return;}
        DamagedBlock damagedBlock = damagedBlockMap.get(e.getBlock());
        if(damagedBlock == null){
            damagedBlock = new DamagedBlock(e.getBlock());
            damagedBlockMap.put(e.getBlock(), damagedBlock);
        }

        float increment = e.getProgress() - damagedBlock.entityDamageMap.getOrDefault(e.getEntity(), 0F);
        if(increment <= 0){return;}
        damagedBlock.debugMessage("BreakProgressUpdateEvent: " + damagedBlock.damage + " += " + increment + " (" + e.getProgress() + " - " + damagedBlock.entityDamageMap.get(e.getEntity()) + ") | " + e.getEntity());

        damagedBlock.damage += increment;
        if(damagedBlock.damage >= 1){
            damagedBlock.brake(e.getEntity());
        } else {
            damagedBlock.sendPacket();
        }
        damagedBlock.entityDamageMap.put(e.getEntity(), e.getProgress());
    }

    @EventHandler
    public static void onBlockBreak(BlockBreakEvent e){
        DamagedBlock damagedBlock = damagedBlockMap.get(e.getBlock());
        if(damagedBlock != null){
            damagedBlock.debugMessage("BreakEvent");
            damagedBlock.delete();
        }
    }

    @EventHandler
    public static void onBlockDamageAbort(BlockDamageAbortEvent e){
        DamagedBlock damagedBlock = damagedBlockMap.get(e.getBlock());
        if(damagedBlock == null){return;}
        damagedBlock.debugMessage("DamageAbortEvent");
        damagedBlock.entityDamageMap.remove(e.getPlayer());
        damagedBlock.sendPacket();
        if(damagedBlock.entityDamageMap.isEmpty()){
            damagedBlock.startFade();
        }
    }

}
