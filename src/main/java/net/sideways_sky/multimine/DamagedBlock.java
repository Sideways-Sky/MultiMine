package net.sideways_sky.multimine;


import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.sideways_sky.multimine.MultiMine.debugMessage;

public class DamagedBlock {
    public static int fadeStartDelay = 40;
    public static int fadeIntervalDelay = 20;
    public static float fadeDamageReduction = 0.1F;
    public DamagedBlock(Block block){
        this.block = block;
        EntityId = Entity.nextEntityId();
        packetTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MultiMine.instance, this::sendDamagedBlockPacket, 1, 1);
        debugMessage("new block | " + this);
    }
    private final int EntityId;
    private final Block block;
    public float damage = 0;
    private final List<Player> activePlayers = new ArrayList<>();
    private final int packetTaskID;
    private int fadeTaskID = -1;
    private int getProgress(){
        return Math.round(damage * 9);
    }
    public void damageTick(Player player){
        float speed = block.getBreakSpeed(player);
        debugMessage("damageTick: " + damage + " + " + speed + " = " + (damage+speed) + " T: " + block.getWorld().getTime()+ " | "+this);
        damage += speed;
        if(damage > 1){
            player.breakBlock(block);
        }
    }

    public void delete(){
        debugMessage("delete block | "+this);
        Bukkit.getScheduler().cancelTask(packetTaskID);
        tryStopFade();
        damage = -1;
        sendDamagedBlockPacket(-1);
        Bukkit.getPluginManager().callEvent(new DamagedBlockDeleteEvent(block, this));
    }
    public void setActive(Player player, boolean active){
        if(active && !activePlayers.contains(player)){
            activePlayers.add(player);
            debugMessage("set active player: " + activePlayers + " | " +this);
            tryStopFade();
        } else if(!active){
            activePlayers.remove(player);
            debugMessage("set inactive player: " + activePlayers + " | " +this);
            if(activePlayers.isEmpty()){
                debugMessage("Starting Fade | "+this);
                fadeTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MultiMine.instance, this::fade, fadeStartDelay, fadeIntervalDelay);
            }
        }
    }
    private void tryStopFade(){
        if(fadeTaskID != -1){
            debugMessage("Stopping Fade | "+this);
            Bukkit.getScheduler().cancelTask(fadeTaskID);
        }
    }
    private void fade(){
        debugMessage("Fade: " + damage + " - " + fadeDamageReduction + " = " + (damage-fadeDamageReduction) + " | " + this);
        damage -= fadeDamageReduction;
        if(damage < 0){
            delete();
        }
    }
    private void sendDamagedBlockPacket(){
        sendDamagedBlockPacket(getProgress());
    }
    private void sendDamagedBlockPacket(int progress){
        ClientboundBlockDestructionPacket packet = new ClientboundBlockDestructionPacket(EntityId,
                new BlockPos(block.getX(), block.getY(), block.getZ()), progress);
        ((CraftServer) Bukkit.getServer()).getHandle().broadcast(null, block.getX(), block.getY(), block.getZ(), 120, ((CraftWorld) block.getWorld()).getHandle().dimension(), packet);
    }
}
