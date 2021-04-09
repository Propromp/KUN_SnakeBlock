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
            return true
        }
        when (args[0]) {
            "help" -> {
                if (sender.hasPermission("snake.help")) {
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
                /snake adhd [動いておけ/動いちゃだめ]:スタートする前にプレイヤーが動けるかどうかを指定
                """.trimIndent()
                    )
                    return true
                } else {
                    sender.sendMessage("権限がありません。")
                }
            }
            "start" -> {
                if (sender.hasPermission("snake.start")) {
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
                } else {
                    sender.sendMessage("権限がありません。")
                }
            }
            "stop" -> {
                if (sender.hasPermission("snake.stop")) {
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
                } else {
                    sender.sendMessage("権限がありません。")
                }
            }
            "startgame" -> {
                if (sender.hasPermission("snake.startgame")) {
                    if (!isStarted) {
                        game = SnakeGame().also { it.start() }
                        sender.sendMessage("スネークゲームを開始します。/snake stopgameで停止できます。")
                        isStarted = true
                        isGameStarted = true
                        return true
                    } else {
                        sender.sendMessage("スネークモードはすでに開始されています。")
                    }
                } else {
                    sender.sendMessage("権限がありません。")
                }
            }
            "stopgame" -> {
                if (sender.hasPermission("snake.stopgame")) {
                    if (isStarted) {
                        game.stop()
                        sender.sendMessage("スネークゲームを終了しました。")
                        isStarted = false
                        isGameStarted = false
                        return true
                    } else {
                        sender.sendMessage("スネークモードはすでに停止されています")
                    }
                } else {
                    sender.sendMessage("権限がありません。")
                }
            }
            "setup" -> {
                if (sender.hasPermission("snake.setup")) {
                    if (args.size < 2) {
                        sender.sendMessage("引数が足りません！")
                        return true
                    }
                    if (args[1].startsWith("@")) {
                        if (sender.hasPermission("snake.selector")) {
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
                        } else {
                            sender.sendMessage("セレクタは許容されません")
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
                } else {
                    sender.sendMessage("権限がありません。")
                }
            }
            "delete" -> {
                if (sender.hasPermission("snake.delete")) {
                    if (args.size < 2) {
                        sender.sendMessage("引数が足りません！")
                        return true
                    }
                    if (args[1].startsWith("@")) {
                        if (sender.hasPermission("snake.selector")) {
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
                            if (Bukkit.selectEntities(sender, args[1]).size == 0) {
                                sender.sendMessage("エンティティが見つかりませんでした。")
                            }
                        } else {
                            sender.sendMessage("セレクタは許容されません")
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
                    sender.sendMessage("エンティティが見つかりませんでした。")
                } else {
                    sender.sendMessage("権限がありません。")
                }
            }
            "yaw" -> {
                sender.sendMessage((sender as Player).location.yaw.toString())
            }
            "pitch" -> {
                sender.sendMessage((sender as Player).location.pitch.toString())
            }
            "adhd" -> {
                if (sender.hasPermission("snake.adhd")) {
                    if (args.size >= 2) {
                        val adhd = when (args[1]) {
                            "動いておけ" -> false
                            else -> true
                        }
                        SnakeBlock.list.forEach {
                            it.adhd = adhd
                        }
                        sender.sendMessage(
                            "adhd設定を「${
                                when (args[1]) {
                                    "動いておけ" -> "動いておけ"
                                    else -> "動いちゃだめ"
                                }
                            }」に変更しました。"
                        )
                    } else {
                        sender.sendMessage("引数が足りません。")
                        return true
                    }
                } else {
                    sender.sendMessage("権限がありません。")
                }
            }
            else -> {
                return false
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
                    "adhd"
                ).filter { sender.hasPermission("snake.$it") }.filter { it.startsWith(args[0], true) }
            }
            2 -> {
                when (args[0]) {
                    "bgm" -> {
                        var res = mutableListOf<String>()
                        if (sender.hasPermission("snake.bgm")) {
                            res.addAll(arrayOf("mario", "polka"))
                        }
                        res
                    }
                    "adhd" -> {
                        var res = mutableListOf<String>()
                        if (sender.hasPermission("snake.adhd")) {
                            res = mutableListOf("動いておけ", "動いちゃだめ")
                        }
                        res
                    }
                    "setup","delete" -> {
                        val res = mutableListOf<String>()
                        if (sender.hasPermission("snake.selector")) {
                            res.addAll(arrayOf("@a", "@p", "@r", "@s"))
                        }
                        if (sender.hasPermission("snake.${args[0]}")) {
                            Bukkit.getOnlinePlayers().forEach {
                                res.add(it.name)
                            }
                        }
                        res
                    }
                    else -> listOf<String>()
                }.filter { it.startsWith(args[1], true) }
            }
            3 -> {
                if (args[0] == "setup" && sender.hasPermission("snake.setup")) {
                    listOf("[スネークブロックが生成される高さ(int)]")
                }
                listOf()
            }
            4 -> {
                if (args[0] == "setup" && sender.hasPermission("snake.setup")) {
                    listOf("true", "false").filter { it.startsWith(args[3], true) }
                }
                listOf()
            }
            5 -> {
                if (args[0] == "setup" && sender.hasPermission("snake.setup")) {
                    listOf("[スネークブロックの長さ(整数)]")
                }
                listOf()
            }
            else -> {
                listOf()
            }
        }
    }
}
