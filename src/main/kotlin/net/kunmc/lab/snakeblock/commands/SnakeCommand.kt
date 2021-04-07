package net.kunmc.lab.snakeblock.commands

import net.kunmc.lab.snakeblock.Main
import net.kunmc.lab.snakeblock.SnakeBlock
import net.kunmc.lab.snakeblock.SnakeGame
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import sun.audio.AudioPlayer
import sun.audio.AudioPlayer.player

class SnakeCommand(private var plugin: Plugin) : CommandExecutor, TabCompleter {
    /**
     * スネークモードがスタートしているか
     */
    var isStarted = false
    var isGameStarted = false
    lateinit var game: SnakeGame
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("引数が足りません")
        }
        when (args[0]) {
            "help" -> {
                sender.sendMessage(
                    """
                /snake help:ヘルプを表示
                /snake start:スネークブロックの処理を開始する
                /snake stop:スネークブロックの処理を停止する
                /snake startgame:スネークゲームを開始する
                /snake stopgame:スネークゲームを停止する
                /snake setup [player] (高さ デフォルト:100) (平面方向しか動けないモードにするかどうか デフォルト:false) (長さ デフォルト:8):指定したプレイヤーにスネークブロックを設定する
                /snake delete [player]:指定したプレイヤーのスネークブロックを削除する
                /snake bgm [polka/mario]:曲の種類を切り替える
                /snake adhd:スタートする前にプレイヤーが動けるかどうかを指定
                """.trimIndent()
                )
                return true
            }
            "start" -> {
                if (!isStarted) {
                    for (sb in SnakeBlock.list) {
                        sb.start()
                    }
                    sender.sendMessage("スネークモードを開始しました。/snake stopで停止できます。")
                    isStarted = true
                    return true
                } else {
                    sender.sendMessage("スネークモードはすでに開始されています。")
                }
            }
            "stop" -> {
                if (isGameStarted) {
                    sender.sendMessage("/snake stopgameで停止してください")
                    return true
                }
                if (isStarted) {
                    for (sb in SnakeBlock.list) {
                        sb.stop()
                    }
                    sender.sendMessage("スネークモードを停止しました。")
                    isStarted = false
                    return true
                } else {
                    sender.sendMessage("スネークモードはすでに停止されています")
                }
            }
            "startgame" -> {
                if (!isStarted) {
                    game = SnakeGame().also { it.start() }
                    sender.sendMessage("スネークゲームを開始します。/snake stopgameで停止できます。")
                    isStarted = true
                    isGameStarted = true
                    return true
                } else {
                    sender.sendMessage("スネークモードはすでに開始されています。")
                }
            }
            "stopgame" -> {
                if (isStarted) {
                    game.stop()
                    sender.sendMessage("スネークゲームを終了しました。")
                    isStarted = false
                    isGameStarted = false
                    return true
                } else {
                    sender.sendMessage("スネークモードはすでに停止されています")
                }
            }
            "setup" -> {
                if (args.size < 2) {
                    sender.sendMessage("引数が足りません！")
                    return true
                }
                if (args[1].startsWith("@")) {
                    Bukkit.selectEntities(sender, args[1]).forEach { entity ->
                        if (entity is Player) {
                            val height = if (args.size >= 3) {
                                args[2].toInt()
                            } else {
                                100
                            }
                            val flat = if (args.size >= 4) {
                                args[3].toBoolean()
                            } else {
                                false
                            }
                            val size = if (args.size >= 5) {
                                args[4].toInt()
                            } else {
                                8
                            }
                            SnakeBlock.list.filter { it.player == entity }.forEach { it.delete() }
                            SnakeBlock(entity, size, height, flat).place()
                            sender.sendMessage("${entity.name}にスネークブロックを設定しました")
                        }
                    }
                    return true
                }
                var player: Player? = null
                if (Bukkit.getPlayer(args[1]) != null) {
                    player = Bukkit.getPlayer(args[1])
                }
                player?.let { player ->
                    val height = if (args.size == 3) {
                        args[2].toInt()
                    } else {
                        100
                    }
                    val flat = if (args.size == 4) {
                        args[3].toBoolean()
                    } else {
                        false
                    }
                    val size = if (args.size == 5) {
                        args[4].toInt()
                    } else {
                        8
                    }
                    SnakeBlock.list.filter { it.player == sender }.forEach { it.delete() }
                    SnakeBlock(player, size, height, flat).place()
                    sender.sendMessage("${player.name}にスネークブロックを設定しました")
                    return true
                }
                sender.sendMessage("${args[1]}が表すプレイヤーは存在しません")
            }
            "delete" -> {
                if (args.size < 2) {
                    sender.sendMessage("引数が足りません！")
                    return true
                }
                if (args[1].startsWith("@")) {
                    Bukkit.selectEntities(sender, args[1]).forEach {
                        if (it is Player) {
                            ArrayList(SnakeBlock.list).forEach { sb ->
                                if (sb.player == it) {
                                    sb.delete()
                                    sender.sendMessage("${it.name}のスネークブロックを削除しました")
                                }
                            }
                        }
                    }
                    return true
                }
                Bukkit.getPlayer(args[1])?.let { player ->
                    ArrayList(SnakeBlock.list).forEach { sb ->
                        if (sb.player == player) {
                            sb.delete()
                            sender.sendMessage("${AudioPlayer.player.name}のスネークブロックを削除しました")
                        }
                    }
                    return true
                }
            }
            "bgm" -> {
                if (args.size >= 2) {
                    Main.hanken = when (args[1]) {
                        "mario" -> true
                        else -> false
                    }
                } else {
                    sender.sendMessage("引数がたりません！")
                    return true
                }
            }
            "yaw" -> {
                sender.sendMessage((sender as Player).location.yaw.toString())
            }
            "pitch" -> {
                sender.sendMessage((sender as Player).location.pitch.toString())
            }
            "adhd" -> {
                if (args.size >= 2) {
                    val adhd = when (args[1]) {
                        "動いておけ" -> false
                        else -> true
                    }
                    SnakeBlock.list.forEach {
                        it.adhd = adhd
                    }
                } else {
                    sender.sendMessage("引数がたりません！")
                    return true
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> {
                listOf(
                    "help",
                    "start",
                    "stop",
                    "setup",
                    "delete",
                    "startgame",
                    "stopgame",
                    "bgm",
                    "adhd"
                ).filter { it.startsWith(args[0], true) }
            }
            2 -> {
                var res = mutableListOf<String>()
                when (args[0]) {
                    "bgm" -> {
                        res = mutableListOf("mario", "polka")
                    }
                    "adhd" -> {
                        res = mutableListOf("動いておけ", "動いちゃだめ")
                    }
                    else -> {
                        res = mutableListOf("@a", "@p", "@r", "@s")
                        Bukkit.getOnlinePlayers().forEach {
                            res.add(it.name)
                        }
                    }
                }
                res.filter { it.startsWith(args[1], true) }
            }
            3 -> {
                listOf("[スネークブロックが生成される高さ(int)]")
            }
            4 -> {
                listOf("true", "false").filter { it.startsWith(args[3], true) }
            }
            5 -> {
                listOf("[スネークブロックの長さ(整数)]")
            }
            else -> {
                listOf()
            }
        }
    }
}