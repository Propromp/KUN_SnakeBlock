package net.kunmc.lab.snakeblock

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftItem
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class SnakeGame:Listener {
    private val br = object:BukkitRunnable(){
        override fun run() {
            spawnCoinAroundPlayer()
        }
    }
    @EventHandler
    fun onPickup(e:PlayerPickupItemEvent){
        if(e.item.itemStack==getCoinItem()){
            e.isCancelled=true
            (e.item as CraftItem).handle.killEntity()
            SnakeBlock.list.forEach {
                if(it.player==e.player){
                    it.addSize()
                    it.player.playSound(it.player.location,"minecraft:snakeblock.pickup",1.0f,1.0f)
                    it.player.playSound(it.player.location,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1.0f,1.0f)
                }
            }
        }
    }
    fun spawnCoinAroundPlayer(){
        SnakeBlock.list.forEach {
            val loc = it.player.location.add(Vector.getRandom().normalize().multiply(4))
            if(it.flat){
                loc.y=it.player.location.y+1
            }
            val item = loc.world.dropItem(loc,getCoinItem())
            item.setGravity(false)
            item.velocity=Vector()
        }
    }
    fun getCoinItem():ItemStack{
        val item = ItemStack(Material.GOLD_INGOT)

        val meta = item.itemMeta
        meta.setCustomModelData(1)
        meta.setDisplayName("SnakeGame")
        meta.setCustomModelData(1)
        item.itemMeta=meta

        return item
    }
    fun start(){
        object :BukkitRunnable(){
            var tick=0
            override fun run() {
                when(tick){
                    0->{
                        Bukkit.getOnlinePlayers().forEach {
                            it.sendTitle("3","ゲーム開始まで",0,30,0)
                            it.playSound(it.location, Sound.BLOCK_DISPENSER_DISPENSE,1f,1f)
                        }
                    }
                    1->{
                        Bukkit.getOnlinePlayers().forEach {
                            it.sendTitle("2","ゲーム開始まで",0,30,0)
                            it.playSound(it.location, Sound.BLOCK_DISPENSER_DISPENSE,1f,1f)
                        }
                    }
                    2->{
                        Bukkit.getOnlinePlayers().forEach {
                            it.sendTitle("1","ゲーム開始まで",0,30,0)
                            it.playSound(it.location, Sound.BLOCK_DISPENSER_DISPENSE,1f,1f)
                        }
                    }
                    3->{
                        Bukkit.getOnlinePlayers().forEach {
                            it.sendTitle("スネークゲーム開始！","",0,30,0)
                            it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP,1f,0.1f)
                        }
                        Bukkit.getPluginManager().registerEvents(this@SnakeGame,Main.plugin)
                        br.runTaskTimer(Main.plugin,0,200)
                        for (sb in SnakeBlock.list) {
                            sb.start()
                        }
                        cancel()
                    }
                }
                tick++
            }
        }.runTaskTimer(Main.plugin,0,20)
    }
    fun stop(){
        PlayerPickupItemEvent.getHandlerList().unregister(this)
        br.cancel()
        for(sb in SnakeBlock.list){
            sb.stop()
        }
    }
}