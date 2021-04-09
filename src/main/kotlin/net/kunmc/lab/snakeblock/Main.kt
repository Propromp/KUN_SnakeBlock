package net.kunmc.lab.snakeblock

import net.kunmc.lab.snakeblock.commands.SnakeCommand
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class Main : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
        var hanken = true
        lateinit var command: SnakeCommand
    }
    override fun onEnable() {
        logger.info("Hi")
        plugin = this
        command=SnakeCommand(this)
        getCommand("snake")!!.setExecutor(command)
        getCommand("snake")!!.tabCompleter= command

        if(Bukkit.getScoreboardManager().mainScoreboard.getObjective("snakeLength")==null){
            Bukkit.getScoreboardManager().mainScoreboard.registerNewObjective("snakeLength","dummy","スネークの長さ")
        }
        if(Bukkit.getScoreboardManager().mainScoreboard.getObjective("snakeTime")==null){
            Bukkit.getScoreboardManager().mainScoreboard.registerNewObjective("snakeTime","dummy","生き残った時間")

        }
    }

    override fun onDisable() {
        logger.info("Bay")
    }
}