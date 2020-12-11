package net.kunmc.snakeblock;

import net.kunmc.snakeblock.commands.SnakeCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Hi");

        getCommand("snake").setExecutor(new SnakeCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("Bay");
    }
}
