package net.sideways_sky.multimine;


import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Player;

import static net.sideways_sky.multimine.MultiMine.debugMessage;

public class DamagedBlock {
    public static int fadeStartDelay = 40;
    public static int fadeIntervalDelay = 20;
    public static float fadeDamageReduction = 0.1F;
    public DamagedBlock(Block block, Player player){
        this.block = block;
        worldLevel = ((CraftWorld) block.getWorld()).getHandle();
        lastPlayer = player;
        EntityId = Entity.nextEntityId();
        packetTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MultiMine.instance, this::sendDamagedBlockPacket, 1, 1);
    }
    private final int EntityId;
    private final Block block;
    private final ServerLevel worldLevel;
    public float damage = 0;
    public Player lastPlayer;
    private final int packetTaskID;
    private int fadeTaskID = -1;
    private int getProgress(){
        return Math.round(damage * 9);
    }
    public void damageTick(){
        float speed = block.getBreakSpeed(lastPlayer);
        debugMessage("damageTick: " + damage + " + " + speed + " = " + damage+speed);
        damage += speed;
        if(damage > 1){
            delete(true);
        }
    }

    public void delete(boolean breakBlock){
        Bukkit.getScheduler().cancelTask(packetTaskID);
        stopFade();
        damage = 0;
        if(breakBlock){
            lastPlayer.breakBlock(block);
        }
        sendDamagedBlockPacket(-1);
        Bukkit.getPluginManager().callEvent(new DamagedBlockDeleteEvent(block, this));
    }
    public void startFade(){
        debugMessage("Starting Fade");
        fadeTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MultiMine.instance, this::fade, fadeStartDelay, fadeIntervalDelay);
    }
    public void stopFade(){
        debugMessage("Stopping Fade");
        if(fadeTaskID != -1){
            Bukkit.getScheduler().cancelTask(fadeTaskID);
        }
    }
    private void fade(){
        damage -= fadeDamageReduction;
        debugMessage("Fade: " + damage);
        if(damage < 0){
            delete(false);
        }
    }
    private void sendDamagedBlockPacket(){
        sendDamagedBlockPacket(getProgress());
    }
    private void sendDamagedBlockPacket(int progress){
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(EntityId,
                new BlockPos(block.getX(), block.getY(), block.getZ()), progress);
        ((CraftServer) Bukkit.getServer()).getHandle().broadcast(null, block.getX(), block.getY(), block.getZ(), 120, worldLevel.dimension(), packet);
    }
}
