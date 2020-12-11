package net.kunmc.snakeblock.commands;

import net.kunmc.snakeblock.SnakeBlock;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;

public class SnakeCommand implements CommandExecutor {

    private static Plugin plugin;

    public SnakeCommand(Plugin plugin1){
        plugin = plugin1;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Class<? extends SnakeCommand> commandClass = this.getClass();
        try {
            return (boolean)commandClass.getMethod(args[0],Player.class,String[].class).invoke(this,(Player)sender,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean help(Player player,String[] args){
        player.sendMessage("/snake help:ヘルプを表示\n" +
                "/snake start:スネークブロックの処理を開始する\n" +
                "/snake stop:スネークブロックの処理を停止する\n" +
                "/snake game start:スネークブロックゲームを開始する\n" +
                "/snake game stop:スネークブロックゲームを終了する");
        return true;
    }
    public static boolean start(Player player,String[] args){
        for(Player p: Bukkit.getOnlinePlayers()){
            new SnakeBlock(plugin,p);
        }
        player.sendMessage("スネークモードを開始しました。/snake stopで停止できます。");
        return true;
    }
    public static boolean stop(Player player,String[] args){
        for(SnakeBlock sb:SnakeBlock.list){
            sb.cancel();
        }
        player.sendMessage("スネークモードを停止しました。");
        return true;
    }


}
