package net.bfcode.bfbase.command.module.chat;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.google.common.collect.Sets;

import net.bfcode.bfbase.BasePlugin;
import net.bfcode.bfbase.command.BaseCommand;
import net.bfcode.bfbase.event.PlayerMessageEvent;
import net.bfcode.bfbase.user.BaseUser;

public class ReplyCommand extends BaseCommand {
	
    private static long VANISH_REPLY_TIMEOUT;
    private BasePlugin plugin;
    
    public ReplyCommand(BasePlugin plugin) {
        super("reply", "Replies to the last conversing player.");
        this.setAliases(new String[] { "r" });
        this.setUsage("/(command) <message>");
        this.plugin = plugin;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable for players.");
            return true;
        }
        Player player = (Player)sender;
        UUID uuid = player.getUniqueId();
        BaseUser baseUser = this.plugin.getUserManager().getUser(uuid);
        UUID lastReplied = baseUser.getLastRepliedTo();
        Player target = ((lastReplied == null) ? null : Bukkit.getPlayer(lastReplied));
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
            if (lastReplied != null && BaseCommand.canSee(sender, target)) {
                sender.sendMessage(ChatColor.RED + "You are in a conversation with " + target.getName() + '.');
            }
            return true;
        }
        long millis = System.currentTimeMillis();
        if (target == null || (!BaseCommand.canSee(sender, target) && millis - baseUser.getLastReceivedMessageMillis() > ReplyCommand.VANISH_REPLY_TIMEOUT)) {
            sender.sendMessage(ChatColor.RED + "There is no player to reply to.");
            return true;
        }
        String message = StringUtils.join((Object[])args, ' ');
		HashSet recipients = Sets.newHashSet((Object[])new Player[] { target });
        PlayerMessageEvent playerMessageEvent = new PlayerMessageEvent(player, recipients, message, false, lastReplied);
        Bukkit.getPluginManager().callEvent((Event)playerMessageEvent);
        if (!playerMessageEvent.isCancelled()) {
            playerMessageEvent.send();
        }
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
    
    static {
        VANISH_REPLY_TIMEOUT = TimeUnit.SECONDS.toMillis(45L);
    }
}
