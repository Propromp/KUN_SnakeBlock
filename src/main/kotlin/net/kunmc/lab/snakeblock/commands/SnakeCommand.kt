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

class SnakeCommand(private var plugin: Plugin) : CommandExecutor,TabCompleter {
    /**
     * スネークモードがスタートしているか
     */
    var isStarted = false
    lateinit var game : SnakeGame
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("引数が足りません")
        }
        when (args[0]) {
            "help" -> {
                sender.sendMessage("""
                /snake help:ヘルプを表示
                /snake start:スネークブロックの処理を開始する
                /snake stop:スネークブロックの処理を停止する
                /snake game start:スネークブロックゲームを開始する
                /snake game stop:スネークブロックゲームを終了する
                """.trimIndent())
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
                    game=SnakeGame().also{it.start()}
                    sender.sendMessage("スネークゲームを開始します。/snake stopgameで停止できます。")
                    isStarted = true
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
                    return true
                } else {
                    sender.sendMessage("スネークモードはすでに停止されています")
                }
            }
            "setup" -> {
                if(args.size<2){
                    sender.sendMessage("引数が足りません！")
                    return true
                }
                if (args[1].startsWith("@")) {
                    Bukkit.selectEntities(sender, args[1]).forEach {
                        if (it is Player) {
                            val height = if(args.size>=3){
                                args[2].toInt()
                            } else {
                                100
                            }
                            val flat = if(args.size>=4){
                                args[3].toBoolean()
                            } else {
                                false
                            }
                            val size = if(args.size>=5){
                                args[4].toInt()
                            } else {
                                8
                            }
                            SnakeBlock(it,size,height, flat).place()
                            sender.sendMessage("${it.name}にスネークブロックを設定しました")
                            return true
                        }
                    }
                }
                Bukkit.getPlayer(args[1])?.let { player ->
                    val height = if(args.size==3){
                        args[2].toInt()
                    } else {
                        100
                    }
                    val flat = if(args.size==4){
                        args[3].toBoolean()
                    } else {
                        false
                    }
                    val size = if(args.size==5){
                        args[4].toInt()
                    } else {
                        8
                    }
                    SnakeBlock(player,size,height, flat).place()
                    sender.sendMessage("${player.name}にスネークブロックを設定しました")
                    return true
                }
                sender.sendMessage("${args[1]}が表すプレイヤーは存在しません")
            }
            "delete" -> {
                if(args.size<2){
                    sender.sendMessage("引数が足りません！")
                    return true
                }
                if (args[1].startsWith("@")) {
                    Bukkit.selectEntities(sender, args[1]).forEach {
                        if (it is Player) {
                            ArrayList(SnakeBlock.list).forEach {sb->
                                if(sb.player==it){
                                    sb.delete()
                                    sender.sendMessage("${it.name}のスネークブロックを削除しました")
                                }
                            }
                        }
                    }
                    return true
                }
                Bukkit.getPlayer(args[1])?.let { player ->
                    ArrayList(SnakeBlock.list).forEach {sb->
                        if(sb.player==player){
                            sb.delete()
                            sender.sendMessage("${AudioPlayer.player.name}のスネークブロックを削除しました")
                        }
                    }
                    return true
                }
            }
            "hanken"->{
                Main.hanken=args[1].toBoolean()
            }
            "yaw" -> {
                sender.sendMessage((sender as Player).location.yaw.toString())
            }
            "pitch" -> {
                sender.sendMessage((sender as Player).location.pitch.toString())
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
        return when(args.size){
            1->{
                listOf("help","start","stop","setup","delete","startgame","stopgame","hanken").filter{it.startsWith(args[0],true)}
            }
            2->{
                var res = mutableListOf<String>()
                when(args[0]) {
                    "hanken"->{
                        res= mutableListOf("true","false")
                    }
                    else-> {
                        res = mutableListOf("@a", "@p", "@r", "@s")
                        Bukkit.getOnlinePlayers().forEach {
                            res.add(it.name)
                        }
                    }
                }
                res.filter{it.startsWith(args[1],true)}
            }
            3->{
                listOf("[スネークブロックが生成される高さ(int)]")
            }
            4->{
                listOf("true","false").filter{it.startsWith(args[3],true)}
            }
            5->{
                listOf("[スネークブロックの長さ(整数)]")
            }
            else->{
                listOf()
            }
        }
    }
}