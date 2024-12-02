package net.sideways_sky.multimine;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DamagedBlock {
    public static int FadeStartDelay = 40;
    public static int FadeIntervalDelay = 20;
    public static float FadeDamageReduction = 0.1F;

    public DamagedBlock(Block block){
        this.block = block;
        entityId = net.minecraft.world.entity.Entity.nextEntityId();
        debugMessage("Created");
    }
    public void debugMessage(String message){
        MultiMine.debugMessage("Block[ " + entityId + " ]: " + message);
    }

    private final int entityId;
    public Map<Entity, Float> entityDamageMap = new HashMap<>();

    private final Block block;
    public void brake(Entity entity){
        if(entity instanceof Player){
            ((Player) entity).breakBlock(block);
        } else {
            block.breakNaturally();
        }
    }

    public float damage = 0;
    private @Nullable ScheduledTask fadeTaskID = null;

    public void startFade(){
        debugMessage("Fade Start");
        fadeTaskID = MultiMine.instance.getServer().getRegionScheduler().runAtFixedRate(MultiMine.instance, block.getLocation(), (e) -> fade(), FadeStartDelay, FadeIntervalDelay);
//                .scheduleSyncRepeatingTask(MultiMine.instance, this::fade, FadeStartDelay, FadeIntervalDelay);
    }
    public void stopFade(){
        if(fadeTaskID != null){
            debugMessage("Fade Stop");
//            Bukkit.getScheduler().cancelTask(fadeTaskID);
//            fadeTaskID = -1;
            fadeTaskID.cancel();
            fadeTaskID = null;
        }
    }

    public void delete(){
        stopFade();
        sendPacket(-1);
        Events.damagedBlockMap.remove(block);
        debugMessage("Deleted");
    }

    public void fade(){
        float preFadeDamageReduction = damage;
        damage -= FadeDamageReduction;
        Consumer<String> message = (messageSuffix) -> debugMessage("Fade: " + damage + " -= " + FadeDamageReduction + messageSuffix);
        if(damage < 0){
            message.accept(" -deleting");
            delete();
        } else if (Math.round(damage * 10F) != Math.round(preFadeDamageReduction * 10F)) {
            message.accept(" -updating");
            sendPacket();
        } else {
            message.accept("");
        }
    }

    public void sendPacket(){
        sendPacket(Math.round(damage * 10F));
    }
    private void sendPacket(int progress){
        ((CraftWorld) block.getWorld()).getHandle().destroyBlockProgress(entityId, ((CraftBlock) block).getPosition(), progress);
    }

}
