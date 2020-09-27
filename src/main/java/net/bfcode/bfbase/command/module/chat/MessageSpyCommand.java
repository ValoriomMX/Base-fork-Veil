package net.bfcode.bfbase.command.module.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Iterables;

import net.bfcode.bfbase.BasePlugin;
import net.bfcode.bfbase.command.BaseCommand;
import net.bfcode.bfbase.user.ServerParticipator;
import net.bfcode.bfbase.util.BukkitUtils;
import net.bfcode.bfbase.util.JavaUtils;
import net.bfcode.bfbase.util.command.CommandArgument;
import net.bfcode.bfbase.util.command.CommandWrapper;

public class MessageSpyCommand extends BaseCommand {
	
    private CommandWrapper handler;
    
    public MessageSpyCommand(BasePlugin plugin) {
        super("socialspy", "Spies on the PM's of a player.");
        this.setUsage("/(command) <list|add|del|clear> [playerName]");
        ArrayList<CommandArgument> arguments = new ArrayList<CommandArgument>(4);
        arguments.add(new MessageSpyListArgument(plugin));
        arguments.add(new IgnoreClearArgument(plugin));
        arguments.add(new MessageSpyAddArgument(plugin));
        arguments.add(new MessageSpyDeleteArgument(plugin));
        Collections.sort(arguments, new CommandWrapper.ArgumentComparator());
        this.handler = new CommandWrapper(arguments);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return this.handler.onCommand(sender, command, label, args);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return this.handler.onTabComplete(sender, command, label, args);
    }
    
    private static class MessageSpyListArgument extends CommandArgument {
    	
        private BasePlugin plugin;
        
        public MessageSpyListArgument(BasePlugin plugin) {
            super("list", "Lists all players you're spying on.");
            this.plugin = plugin;
        }
        
        @Override
        public String getUsage(String label) {
            return "/" + label + ' ' + this.getName();
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            ServerParticipator participator = this.plugin.getUserManager().getParticipator(sender);
            if (participator == null) {
                sender.sendMessage(ChatColor.RED + "You are not able to message spy.");
                return true;
            }
            LinkedHashSet<String> spyingNames = new LinkedHashSet<String>();
            Set<String> messageSpying = participator.getMessageSpying();
            if (messageSpying.size() == 1 && ((String)Iterables.getOnlyElement((Iterable)messageSpying)).equals("all")) {
                sender.sendMessage(ChatColor.GRAY + "You are currently spying on the messages of all players.");
                return true;
            }
            for (String spyingId : messageSpying) {
                String name = Bukkit.getOfflinePlayer(UUID.fromString(spyingId)).getName();
                if (name == null) {
                    continue;
                }
                spyingNames.add(name);
            }
            if (spyingNames.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "You are not spying on the messages of any players.");
                return true;
            }
            sender.sendMessage(ChatColor.GRAY + "You are currently spying on the messages of (" + spyingNames.size() + " players): " + ChatColor.RED + StringUtils.join((Collection)spyingNames, ChatColor.GRAY.toString() + ", " + ChatColor.RED) + ChatColor.GRAY + '.');
            return true;
        }
    }
    
    private static class IgnoreClearArgument extends CommandArgument {
    	
        private BasePlugin plugin;
        
        public IgnoreClearArgument(BasePlugin plugin) {
            super("clear", "Clears your current spy list.");
            this.plugin = plugin;
        }
        
        @Override
        public String getUsage(String label) {
            return "/" + label + ' ' + this.getName();
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            ServerParticipator participator = this.plugin.getUserManager().getParticipator(sender);
            if (participator == null) {
                sender.sendMessage(ChatColor.RED + "You are not able to message spy.");
                return true;
            }
            participator.getMessageSpying().clear();
            sender.sendMessage(ChatColor.YELLOW + "You are no longer spying the messages of anyone.");
            return true;
        }
    }
    
    private static class MessageSpyAddArgument extends CommandArgument
    {
        private BasePlugin plugin;
        
        public MessageSpyAddArgument(BasePlugin plugin) {
            super("add", "Adds a player to your message spy list.");
            this.plugin = plugin;
        }
        
        @Override
        public String getUsage(String label) {
            return "/" + label + ' ' + this.getName() + " <all|playerName>";
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            ServerParticipator participator = this.plugin.getUserManager().getParticipator(sender);
            if (participator == null) {
                sender.sendMessage(ChatColor.RED + "You are not able to message spy.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            Set<String> messageSpying = participator.getMessageSpying();
            boolean all = messageSpying.contains("all");
            if (all || JavaUtils.containsIgnoreCase(messageSpying, args[1])) {
                sender.sendMessage(ChatColor.RED + "You are already spying on the messages of " + (all ? "all players" : args[1]) + '.');
                return true;
            }
            if (args[1].equalsIgnoreCase("all")) {
                messageSpying.clear();
                messageSpying.add("all");
                sender.sendMessage(ChatColor.GREEN + "You are now spying on the messages of all players.");
                return true;
            }
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[1]);
            if (!offlineTarget.hasPlayedBefore() && offlineTarget.getPlayer() == null) {
                sender.sendMessage(ChatColor.GOLD + "Player '" + ChatColor.WHITE + args[1] + ChatColor.GOLD + "' not found.");
                return true;
            }
            if (offlineTarget.equals(sender)) {
                sender.sendMessage(ChatColor.RED + "You cannot spy on the messages of yourself.");
                return true;
            }
            sender.sendMessage(ChatColor.YELLOW + "You are " + (messageSpying.add(offlineTarget.getUniqueId().toString()) ? (ChatColor.GREEN + "now") : (ChatColor.RED + "already")) + ChatColor.YELLOW + " spying on the messages of " + offlineTarget.getName() + '.');
            return true;
        }
        
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return (args.length == 2) ? null : Collections.emptyList();
        }
    }
    
    private static class MessageSpyDeleteArgument extends CommandArgument
    {
        private BasePlugin plugin;
        
        public MessageSpyDeleteArgument(BasePlugin plugin) {
            super("delete", "Deletes a player from your message spy list.");
            this.plugin = plugin;
            this.aliases = new String[] { "del", "remove" };
        }
        
        @Override
        public String getUsage(String label) {
            return "/" + label + ' ' + this.getName() + " <playerName>";
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            ServerParticipator participator = this.plugin.getUserManager().getParticipator(sender);
            if (participator == null) {
                sender.sendMessage(ChatColor.RED + "You are not able to message spy.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            Set<String> messageSpying = participator.getMessageSpying();
            if (args[1].equalsIgnoreCase("all")) {
                messageSpying.remove("all");
                sender.sendMessage(ChatColor.RED + "You are no longer spying on the messages of all players.");
                return true;
            }
            OfflinePlayer offlineTarget = BukkitUtils.offlinePlayerWithNameOrUUID(args[1]);
            if (!offlineTarget.hasPlayedBefore() && !offlineTarget.isOnline()) {
                sender.sendMessage(ChatColor.GOLD + "Player named or with UUID '" + ChatColor.WHITE + args[1] + ChatColor.GOLD + "' not found.");
                return true;
            }
            sender.sendMessage("You are " + (messageSpying.remove(offlineTarget.getUniqueId().toString()) ? (ChatColor.GREEN + "no longer") : (ChatColor.RED + "still not")) + ChatColor.YELLOW + " spying on the messages of " + offlineTarget.getName() + '.');
            return true;
        }
        
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return (args.length == 2) ? null : Collections.emptyList();
        }
    }
}
