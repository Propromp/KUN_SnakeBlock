package net.kunmc.snakeblock;

import net.kunmc.snakeblock.util.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class SnakeBlock implements Listener{
    private Player player;
    private FallingBlock front,rear;
    private int taskId;
    public static List<SnakeBlock> list = new ArrayList<>();
    private List<Block> blockList = new ArrayList<>();
    private Plugin plugin;
    private BukkitTask br;

    public SnakeBlock(Plugin plugin,Player player){
        plugin.getServer().getPluginManager().registerEvents(this,plugin);

        this.plugin = plugin;

        this.list.add(this);

        taskId = (int) (Math.random()*10000);

        //先頭と後尾のエンティティを設定
        front = player.getWorld().spawnFallingBlock(player.getLocation().getBlock().getLocation().add(0,-1,0.5), Material.BEDROCK.createBlockData());
        rear = player.getWorld().spawnFallingBlock(player.getLocation().getBlock().getLocation().add(-4,-1,0.5), Material.BEDROCK.createBlockData());
        front.setGravity(false);
        front.setInvulnerable(true);
        rear.setGravity(false);
        rear.setInvulnerable(true);

        ((CraftEntity)front).getHandle().noclip=true;//当たり判定を無効化
        ((CraftEntity)rear).getHandle().noclip=true;

        ((CraftEntity) front).getHandle().ticksLived = 1;
        ((CraftEntity) rear).getHandle().ticksLived = 1;

        for(int i = 0; i < 4;i++){
            player.getLocation().add(-i,-1,0).getBlock().setType(Material.BEDROCK);
            blockList.add(player.getLocation().add(-i,-1,0).getBlock());
        }

        this.player = player;

        br = new BukkitRunnable() {
            @Override
            public void run() {
                fill();
            }
        }.runTaskTimer(plugin,0,20);
    }

    private void fill(){
        FallingBlock oldFront = front;
        front = player.getWorld().spawnFallingBlock(oldFront.getLocation().getBlock().getLocation().add(0.5,0,0.5),oldFront.getBlockData());
        oldFront.remove();

        FallingBlock oldRear = rear;
        rear = player.getWorld().spawnFallingBlock(oldRear.getLocation().getBlock().getLocation().add(0.5,0,0.5),oldRear.getBlockData());
        oldRear.remove();


        front.getLocation().getBlock().setType(Material.BEDROCK);
        blockList.add(front.getLocation().getBlock());
        rear.getLocation().getBlock().setType(Material.AIR);
        blockList.remove(rear.getLocation().getBlock());



        front.setGravity(false);
        front.setInvulnerable(true);
        rear.setGravity(false);
        rear.setInvulnerable(true);
        ((CraftEntity)front).getHandle().noclip=true;//当たり判定を無効化
        ((CraftEntity)rear).getHandle().noclip=true;


        Vector frontVec = getDirection(player.getLocation()).multiply(0.06);

        Vector rearVec = new Vector();//後尾のエンティティが進む方向
        for(BlockFace face : new BlockFace[]{
                BlockFace.UP,
                BlockFace.DOWN,
                BlockFace.NORTH,
                BlockFace.SOUTH,
                BlockFace.WEST,
                BlockFace.EAST,
        }){
            Block relative = rear.getLocation().getBlock().getRelative(face);
            plugin.getLogger().info(relative.getType().toString());
            if(relative.getType() == Material.BEDROCK){
                rearVec = face.getDirection().multiply(0.06);
            }
        }

        front.setVelocity(frontVec);
        rear.setVelocity(rearVec);
    }

    public void cancel(){
        front.remove();
        rear.remove();

        for(Block block:blockList){
            block.setType(Material.AIR);
        }
        this.getBr().cancel();
    }

    public void onEntityDamage(EntityDamageEvent e){
        if(e.getEntityType() == EntityType.FALLING_BLOCK){
            e.setCancelled(true);
        }
    }

    public BukkitTask getBr() {
        return br;
    }

    private Vector getDirection(Location loc){
        if(45 < loc.getPitch()){
            return new Vector(0,-1,0);
        } else if(loc.getPitch() < -45){
            return new Vector(0,1,0);
        } else {
            if(135 <= loc.getYaw() || loc.getYaw() < -135){
                return new Vector(0,0,-1);
            } else if(-135 <= loc.getYaw() && loc.getYaw() < -45){
                return new Vector(1,0,0);
            } else if(-45 <= loc.getYaw() && loc.getYaw() < 45){
                return new Vector(0,0,1);
            } else if(45 <= loc.getYaw() && loc.getYaw() < 135){
                return new Vector(-1,0,0);
            }
        }
        return new Vector();
    }
}
