package net.kunmc.lab.snakeblock

import com.destroystokyo.paper.Title
import org.bukkit.*
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftItem
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.bukkit.Bukkit.getScoreboardManager as sm

class SnakeGame : Listener {
    private val br = object : BukkitRunnable() {
        override fun run() {
            spawnCoinAroundPlayer()
        }
    }
    private val br2 = object : BukkitRunnable() {
        override fun run() {
            if (SnakeBlock.list.size == 1) {//勝利処理
                val sb = SnakeBlock.list[0]
                ArrayList(Bukkit.getOnlinePlayers()).also {
                    it.remove(sb.player)
                }.forEach { player ->
                    player.gameMode = GameMode.SPECTATOR
                    player.teleport(sb.player.location)
                    player.sendTitle(Title("${ChatColor.GOLD}${sb.player.name}の勝利",
                        "生き延びた時間:${
                            sm().mainScoreboard.getObjective("snakeTime")!!.getScore(sb.player)
                        } ブロック数:${sb.size}"))
                }
                object : BukkitRunnable() {
                    var tick = 0
                    override fun run() {
                        if(tick<200) {
                            val fw = sb.player.world.spawnEntity(sb.player.location.add(
                                Vector.getRandom().normalize().multiply(2).rotateAroundY(Math.random().times(360))
                                    .rotateAroundZ(Math.random().times(360))), EntityType.FIREWORK) as Firework
                            val meta = fw.fireworkMeta
                            meta.addEffect(FireworkEffect.builder().withColor(Color.ORANGE).withFade(Color.RED).build())
                            meta.power = 1
                            fw.fireworkMeta = meta
                            object : BukkitRunnable() {
                                override fun run() {
                                    fw.detonate()
                                }
                            }.runTaskLater(Main.plugin, 10)
                        } else {
                            cancel()
                        }
                        tick+=5
                    }
                }.runTaskTimer(Main.plugin,0,5)

                sb.player.sendTitle(Title("${ChatColor.GOLD}VICTORY"))
                sb.player.playSound(sb.player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.1f)

                stop()
            }
        }
    }

    @EventHandler
    fun onPickup(e: PlayerPickupItemEvent) {
        if (e.item.itemStack == getCoinItem()) {
            e.isCancelled = true
            (e.item as CraftItem).handle.killEntity()
            SnakeBlock.list.forEach {
                if (it.player == e.player) {
                    it.addSize()
                    it.player.playSound(it.player.location, "minecraft:snakeblock.pickup", 1.0f, 1.0f)
                    it.player.playSound(it.player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
                }
            }
        }
    }

    fun spawnCoinAroundPlayer() {
        SnakeBlock.list.forEach {
            val loc = it.player.location.add(Vector.getRandom().normalize().multiply(4))
            if (it.flat) {
                loc.y = it.player.location.y + 1
            }
            val item = loc.world.dropItem(loc, getCoinItem())
            item.setGravity(false)
            item.velocity = Vector()
        }
    }

    fun getCoinItem(): ItemStack {
        val item = ItemStack(Material.GOLD_INGOT)

        val meta = item.itemMeta
        meta.setCustomModelData(1)
        meta.setDisplayName("SnakeGame")
        meta.setCustomModelData(1)
        item.itemMeta = meta

        return item
    }

    fun start() {
        object : BukkitRunnable() {
            var tick = 0
            override fun run() {
                when (tick) {
                    0 -> {
                        Bukkit.getOnlinePlayers().forEach {
                            it.sendTitle("3", "ゲーム開始まで", 0, 30, 0)
                            it.playSound(it.location, Sound.BLOCK_DISPENSER_DISPENSE, 1f, 1f)
                        }
                    }
                    1 -> {
                        Bukkit.getOnlinePlayers().forEach {
                            it.sendTitle("2", "ゲーム開始まで", 0, 30, 0)
                            it.playSound(it.location, Sound.BLOCK_DISPENSER_DISPENSE, 1f, 1f)
                        }
                    }
                    2 -> {
                        Bukkit.getOnlinePlayers().forEach {
                            it.sendTitle("1", "ゲーム開始まで", 0, 30, 0)
                            it.playSound(it.location, Sound.BLOCK_DISPENSER_DISPENSE, 1f, 1f)
                        }
                    }
                    3 -> {
                        Bukkit.getOnlinePlayers().forEach {
                            it.sendTitle("スネークゲーム開始！", "コンパスを持って右クリックで進路を変更できます", 0, 30, 0)
                            it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.1f)
                        }
                        Bukkit.getPluginManager().registerEvents(this@SnakeGame, Main.plugin)
                        br.runTaskTimer(Main.plugin, 0, 200)
                        br2.runTaskTimer(Main.plugin, 0, 5)
                        for (sb in SnakeBlock.list) {
                            sb.start()
                        }
                        cancel()
                    }
                }
                tick++
            }
        }.runTaskTimer(Main.plugin, 0, 20)
    }

    fun stop() {
        PlayerPickupItemEvent.getHandlerList().unregister(this)
        br.cancel()
        br2.cancel()
        for (sb in SnakeBlock.list) {
            sb.stop()
        }
    }
}