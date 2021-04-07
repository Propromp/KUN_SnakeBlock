package net.kunmc.lab.snakeblock

import com.destroystokyo.paper.Title
import com.xxmicloxx.NoteBlockAPI.model.RepeatMode
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder
import net.kunmc.lab.snakeblock.Main.Companion.plugin
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.io.File
import org.bukkit.Bukkit.getScoreboardManager as sm

class SnakeBlock(val player: Player, var size: Int, val height: Int, val flat: Boolean) : Listener {
    var adhd: Boolean = true//adhd防止
    var blocks = mutableListOf<Block>()

    lateinit var front: ArmorStand
    lateinit var rear: ArmorStand
    private var rsp: RadioSongPlayer
    private lateinit var br: BukkitRunnable
    private lateinit var partBr: BukkitRunnable
    private val period: Int = 10;
    private var direction = getPlayerDirection()
    private var killMessage: String? = null

    init {
        var song = NBSDecoder.parse(File(plugin.dataFolder, "hankenok.nbs"))
        if (Main.hanken) {
            song = NBSDecoder.parse(File(plugin.dataFolder, "hankenng.nbs"))
        }
        rsp = RadioSongPlayer(song)
        rsp.addPlayer(player)
        rsp.category = SoundCategory.RECORDS
        sm().mainScoreboard.getObjective("snakeLength")!!.getScore(player).score=size
    }

    fun addSize() {
        blocks.add(0, blocks[0].location.subtract(blocks[1].location.subtract(blocks[0].location)).block)
        blocks[0].type = Material.BEDROCK
        size=blocks.size;
        player.sendMessage("あなたのスネークブロックが1ブロック長くなりました。")
        sm().mainScoreboard.getObjective("snakeLength")!!.getScore(player).score=size
    }

    fun place() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        player.teleport(Location(player.world, player.location.x, (height + 1).toDouble(), player.location.z))
        val loc = player.location.subtract(0.0, 1.0, 0.0)
        for (i in 1..size) {
            loc.block.type = Material.BEDROCK
            blocks.add(0, loc.block)
            loc.subtract(player.facing.direction)
        }
        direction=player.facing.direction
        list.add(this)
        partBr = object : BukkitRunnable() {
            override fun run() {
                for (i in 0..20) {
                    player.world.spawnParticle(Particle.COMPOSTER, blocks[blocks.lastIndex].location.add(0.5,0.5,0.5).add(
                        Vector.getRandom().normalize().rotateAroundY(Math.random().times(360))
                            .rotateAroundZ(Math.random().times(360))), 1)
                }
            }
        }
        partBr.runTaskTimer(plugin, 0, 5)
    }

    fun delete() {
        PlayerInteractEvent.getHandlerList().unregister(this)
        PlayerDeathEvent.getHandlerList().unregister(this)
        PlayerDropItemEvent.getHandlerList().unregister(this)
        PlayerMoveEvent.getHandlerList().unregister(this)
        blocks.forEach {
            it.type = Material.AIR
        }
        blocks = mutableListOf()
        list.remove(this)
        partBr.cancel()
    }

    fun start() {
        sm().mainScoreboard.getObjective("snakeTime")!!.getScore(player).score=0
        player.sendMessage("コンパスを持ってクリックで進路を変更できます！")
        player.inventory.addItem(player.inventory.itemInMainHand)
        player.inventory.setItemInMainHand(ItemStack(Material.COMPASS))
        //BGM
        rsp.isPlaying = true
        rsp.repeatMode = RepeatMode.ALL
        //frontとrearの設置
        front = player.world.spawnEntity(blocks[blocks.lastIndex].location.add(0.5, 1.0, 0.5),
            EntityType.ARMOR_STAND) as ArmorStand
        front.ticksLived = 1
        front.setGravity(false)
        front.isVisible = false
        front.setItem(EquipmentSlot.HEAD, ItemStack(Material.BEDROCK))
        rear = player.world.spawnEntity(blocks[0].location.add(0.5, 1.0, 0.5), EntityType.ARMOR_STAND) as ArmorStand
        rear.ticksLived = 1
        rear.setGravity(false)
        rear.isVisible = false
        rear.setItem(EquipmentSlot.HEAD, ItemStack(Material.BEDROCK))
        br = object : BukkitRunnable() {
            var useVec=false
            override fun run() {
                //進行方向処理
                if(useVec&&vector!=null){
                    direction=vector!!
                    useVec=false
                    vector=null
                }
                if(vector!=null){
                    useVec=true
                }
                val newBlock = blocks[blocks.lastIndex].location.add(direction).block
                val oldBlock = blocks[0]
                //Scoreboard処理
                sm().mainScoreboard.getObjective("snakeTime")!!.getScore(player).score+=period
                //コンパス処理
                player.compassTarget = player.location.add(direction.clone().multiply(10))
                //落ちた判定
                var otita = true
                blocks.forEach {
                    if (player.location.y >= it.location.y) {
                        otita = false
                    }
                }
                if (otita) {
                    killMessage = "${player.name}はスネークブロックから転げ落ちた"
                    (player as CraftPlayer).handle.killEntity()
                }
                //にゅるにゅる処理
                front.teleport(newBlock.location.add(0.5, 1.0, 0.5))
                front.ticksLived = 1
                rear.teleport(blocks[1].location.add(0.5, 1.0, 0.5))
                rear.ticksLived = 1
                //衝突判定
                if (newBlock.type != Material.AIR) {
                    killMessage = "${player.name}は壁に衝突した。"
                    (player as CraftPlayer).handle.killEntity()
                }
                //進行処理
                newBlock.type = Material.BARRIER
                blocks[blocks.lastIndex].type = Material.BEDROCK
                oldBlock.type = Material.AIR
                blocks.add(newBlock)
                blocks.remove(oldBlock)
            }
        }
        br.runTaskTimer(plugin, 0, period.toLong())
    }

    var dead = false
    fun kill() {
        val newBlock = blocks[blocks.lastIndex]
        object : BukkitRunnable() {
            var tick = 0
            override fun run() {
                when {
                    tick==0 -> {
                        dead=true
                    }
                    tick < 20 -> {
                        newBlock.world.spawnParticle(Particle.EXPLOSION_HUGE, newBlock.location, 10)
                        newBlock.world.playSound(newBlock.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
                    }
                    else -> {
                        stop()
                        delete()
                        cancel()
                    }
                }
                tick++
            }
        }.runTaskTimer(plugin, 0, 1)
    }

    fun stop() {
        if (rsp.isPlaying) {
            rsp.isPlaying = false
            br.cancel()
            front.remove()
            rear.remove()
        }
    }

    private fun getPlayerDirection(): Vector {
        val pitch = player.location.pitch
        var direction = if (!flat && 30 < pitch) {
            Vector(0, -1, 0)
        } else if (!flat && pitch < -30) {
            Vector(0, 1, 0)
        } else {
            player.facing.direction
        }
        if (blocks.isNotEmpty()) {
            if (blocks[blocks.lastIndex].location.add(direction).block.type != Material.AIR) {
                direction = this.direction
            }
        }
        return direction
    }
    private var vector:Vector?=null
    @EventHandler
    fun onClick(e: PlayerInteractEvent) {
        if (e.player.inventory.itemInMainHand.type == Material.COMPASS && e.player==player) {
            vector=this.direction.clone()
            direction = getPlayerDirection()
            player.sendActionBar("進行方向を${
                when (direction) {
                    Vector(0, -1, 0) -> {
                        vector=null
                        "下"
                    }
                    Vector(0, 1, 0) -> {
                        "上"
                    }
                    else -> {
                        vector=null
                        player.facing.name
                    }
                }
            }に変更しました。")
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        if (e.entity == player) {
            if(!dead) {
                killMessage?.let {
                    e.deathMessage = it
                }
                kill()
            }
        }
    }
    @EventHandler
    fun onDrop(e:PlayerDropItemEvent){
        if(e.itemDrop.itemStack.type==Material.COMPASS){
            e.isCancelled=true
        }
    }
    @EventHandler
    fun onMove(e:PlayerMoveEvent){
        if(!rsp.isPlaying&&e.player==player&&adhd){
            e.isCancelled=true
        }
    }

    companion object {
        val list = mutableListOf<SnakeBlock>()
    }
}