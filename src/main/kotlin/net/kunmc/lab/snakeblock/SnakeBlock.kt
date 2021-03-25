package net.kunmc.lab.snakeblock

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode
import com.xxmicloxx.NoteBlockAPI.model.Song
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder
import net.kunmc.lab.snakeblock.Main.Companion.plugin
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity
import net.kunmc.lab.snakeblock.SnakeBlock
import net.kunmc.lab.snakeblock.commands.SnakeCommand
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import org.bukkit.util.io.BukkitObjectInputStream
import java.io.File
import java.lang.IllegalAccessException
import java.lang.reflect.InvocationTargetException
import java.lang.NoSuchMethodException
import java.util.ArrayList

class SnakeBlock(val player: Player, var size:Int, val height:Int, val flat:Boolean) {
    var blocks = mutableListOf<Block>()

    lateinit var front:ArmorStand
    lateinit var rear:ArmorStand
    private var rsp:RadioSongPlayer
    private lateinit var br:BukkitRunnable
    private val period:Int = 10;
    init {
        var song = NBSDecoder.parse(File(plugin.dataFolder,"hankenok.nbs"))
        if(Main.hanken){
            song = NBSDecoder.parse(File(plugin.dataFolder,"hankenng.nbs"))
        }
        rsp = RadioSongPlayer(song)
        rsp.addPlayer(player)
    }

    fun addSize(){
        blocks.add(0,blocks[0].location.subtract(blocks[1].location.subtract(blocks[0].location)).block)
        blocks[0].type=Material.BEDROCK
        player.sendMessage("あなたのスネークブロックが1ブロック長くなりました。")
    }
    fun place(){
        player.teleport(Location(player.world,player.location.x,(height+1).toDouble(),player.location.z))
        val loc = player.location.subtract(0.0,1.0,0.0)
        for(i in 1..size){
            loc.block.type=Material.BEDROCK
            blocks.add(0,loc.block)
            loc.subtract(player.facing.direction)
        }
        list.add(this)
    }
    fun delete(){
        blocks.forEach {
            it.type=Material.AIR
        }
        blocks= mutableListOf()
        list.remove(this)
    }
    fun start(){
        //BGM
        rsp.isPlaying = true
        rsp.repeatMode=RepeatMode.ALL

        //frontとrearの設置
        front=player.world.spawnEntity(blocks[blocks.lastIndex].location.add(0.5,1.0,0.5),EntityType.ARMOR_STAND) as ArmorStand
        front.ticksLived=1
        front.setGravity(false)
        front.isVisible=false
        front.setItem(EquipmentSlot.HEAD, ItemStack(Material.BEDROCK))
        rear=player.world.spawnEntity(blocks[0].location.add(0.5,1.0,0.5),EntityType.ARMOR_STAND) as ArmorStand
        rear.ticksLived=1
        rear.setGravity(false)
        rear.isVisible=false
        rear.setItem(EquipmentSlot.HEAD, ItemStack(Material.BEDROCK))
        br=object:BukkitRunnable() {
            override fun run() {
                val newBlock = blocks[blocks.lastIndex].location.add(getDirection()).block
                val oldBlock = blocks[0]
                //にゅるにゅる処理
                front.teleport(newBlock.location.add(0.5,1.0,0.5))
                front.ticksLived=1
                rear.teleport(blocks[1].location.add(0.5,1.0,0.5))
                rear.ticksLived=1
                //衝突判定
                if(newBlock.type!=Material.AIR){
                    object:BukkitRunnable() {
                        var tick = 0
                        override fun run() {
                            if(tick<20) {
                                newBlock.world.spawnParticle(Particle.EXPLOSION_HUGE, newBlock.location, 10)
                                newBlock.world.playSound(newBlock.location, Sound.ENTITY_GENERIC_EXPLODE,1f,1f)
                            } else {
                                stop()
                                delete()
                                cancel()
                            }
                            tick++
                        }
                    }.runTaskTimer(plugin,0,1)
                }
                //進行処理
                newBlock.type = Material.BARRIER
                blocks[blocks.lastIndex].type=Material.BEDROCK
                oldBlock.type = Material.AIR
                blocks.add(newBlock)
                blocks.remove(oldBlock)
            }
        }
        br.runTaskTimer(plugin,0,period.toLong())
    }
    fun stop(){
        if(rsp.isPlaying) {
            rsp.isPlaying = false
            br.cancel()
            front.remove()
            rear.remove()
        }
    }
    private fun getDirection(): Vector {
        val pitch = player.location.pitch
        return if (!flat && 45 <pitch) {
            Vector(0, -1, 0)
        } else if (!flat && pitch < -45) {
            Vector(0, 1, 0)
        } else{
            player.facing.direction
        }
    }
    companion object {
        val list = mutableListOf<SnakeBlock>()
    }
}