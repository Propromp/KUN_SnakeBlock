package net.kunmc.lab.snakeblock

import net.kunmc.lab.snakeblock.commands.SnakeCommand
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class Main : JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
        var hanken = true
    }
    override fun onEnable() {
        logger.info("Hi")
        plugin = this
        getCommand("snake")!!.setExecutor(SnakeCommand(this))
        getCommand("snake")!!.tabCompleter=SnakeCommand(this)

        val bgmFile1 = File(dataFolder,"hankenng.nbs")
        if(!bgmFile1.exists()){
            logger.info("downloading hankenng.nbs")
            bgmFile1.parentFile.mkdirs()
            val url = URL("https://github.com/Propromp/snake_bgm/raw/master/mario.nbs")
            val conn = url.openConnection() as HttpURLConnection
            val outStream = FileOutputStream(bgmFile1)
            outStream.write(conn.inputStream.readBytes())
            outStream.flush()
            outStream.close()
            logger.info("complete.")
        }
        val bgmFile2 = File(dataFolder,"hankenok.nbs")
        if(!bgmFile2.exists()){
            logger.info("downloading hankenok.nbs")
            bgmFile2.parentFile.mkdirs()
            val url = URL("https://github.com/Propromp/snake_bgm/raw/master/polka.nbs")
            val conn = url.openConnection() as HttpURLConnection
            val outStream = FileOutputStream(bgmFile2)
            outStream.write(conn.inputStream.readBytes())
            outStream.flush()
            outStream.close()
            logger.info("complete.")
        }
    }

    override fun onDisable() {
        logger.info("Bay")
    }
}