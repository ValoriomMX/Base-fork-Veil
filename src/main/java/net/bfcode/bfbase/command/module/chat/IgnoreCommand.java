package net.bfcode.bfbase.command.module.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.bfcode.bfbase.BaseConstants;
import net.bfcode.bfbase.BasePlugin;
import net.bfcode.bfbase.command.BaseCommand;
import net.bfcode.bfbase.staffmode.StaffPriority;
import net.bfcode.bfbase.user.BaseUser;
import net.bfcode.bfbase.util.BukkitUtils;
import net.bfcode.bfbase.util.command.CommandArgument;
import net.bfcode.bfbase.util.command.CommandWrapper;

public class IgnoreCommand extends BaseCommand {
	
    private CommandWrapper handler;
    
    public IgnoreCommand(BasePlugin plugin) {
        super("ignore", "Ignores a player from messages.");
        this.setUsage("/(command) <list|add|del|clear> [playerName]");
        ArrayList<CommandArgument> arguments = new ArrayList<CommandArgument>(4);
        arguments.add(new IgnoreClearArgument(plugin));
        arguments.add(new IgnoreListArgument(plugin));
        arguments.add(new IgnoreAddArgument(plugin));
        arguments.add(new IgnoreDeleteArgument(plugin));
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
    
    private static class IgnoreDeleteArgument extends CommandArgument {
    	
        private BasePlugin plugin;
        
        public IgnoreDeleteArgument(BasePlugin plugin) {
            super("delete", "Un-ignores a player.");
            this.plugin = plugin;
            this.aliases = new String[] { "del", "remove", "unset" };
        }
        
        @Override
        public String getUsage(String label) {
            return "/" + label + ' ' + this.getName() + " <playerName>";
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: " + this.getUsage(label));
                return true;
            }
            sender.sendMessage(ChatColor.YELLOW + "You are " + (this.plugin.getUserManager().getUser(((Player)sender).getUniqueId()).getIgnoring().remove(args[1]) ? (ChatColor.RED + "not") : (ChatColor.GREEN + "no longer")) + ChatColor.YELLOW + " ignoring " + args[1] + '.');
            return true;
        }
        
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return null;
        }
    }
    
    private static class IgnoreListArgument extends CommandArgument {
    	
        private BasePlugin plugin;
        
        public IgnoreListArgument(BasePlugin plugin) {
            super("list", "Lists all ignored players.");
            this.plugin = plugin;
        }
        
        @Override
        public String getUsage(String label) {
            return "/" + label + ' ' + this.getName();
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
                return true;
            }
            Set<String> ignoring = this.plugin.getUserManager().getUser(((Player)sender).getUniqueId()).getIgnoring();
            if (ignoring.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "You are not ignoring anyone.");
                return true;
            }
            sender.sendMessage(ChatColor.YELLOW + "You are ignoring (" + ignoring.size() + ") members: " + '[' + ChatColor.WHITE + StringUtils.join((Collection)ignoring, ", ") + ChatColor.YELLOW + ']');
            return true;
        }
        
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return Collections.emptyList();
        }
    }
    
    private static class IgnoreClearArgument extends CommandArgument {
    	
        private BasePlugin plugin;
        
        public IgnoreClearArgument(BasePlugin plugin) {
            super("clear", "Clears all ignored players.");
            this.plugin = plugin;
        }
        
        @Override
        public String getUsage(String label) {
            return "/" + label + ' ' + this.getName();
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
                return true;
            }
            Set<String> ignoring = this.plugin.getUserManager().getUser(((Player)sender).getUniqueId()).getIgnoring();
            if (ignoring.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Your ignore list is already empty.");
                return true;
            }
            ignoring.clear();
            sender.sendMessage(ChatColor.YELLOW + "Your ignore list has been cleared.");
            return true;
        }
        
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return Collections.emptyList();
        }
    }
    
    private static class IgnoreAddArgument extends CommandArgument {
    	
        private BasePlugin plugin;
        
        public IgnoreAddArgument(BasePlugin plugin) {
            super("add", "Starts ignoring a player.");
            this.plugin = plugin;
        }
        
        @Override
        public String getUsage(String label) {
            return "/" + label + ' ' + this.getName() + " <playerName>";
        }
        
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(this.getUsage(label));
                return true;
            }
            Player player = (Player)sender;
            UUID uuid = player.getUniqueId();
            BaseUser baseUser = this.plugin.getUserManager().getUser(uuid);
            Set<String> ignoring = baseUser.getIgnoring();
            Player target = BukkitUtils.playerWithNameOrUUID(args[1]);
            if (target == null || !BaseCommand.canSee(sender, target)) {
                sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[1]));
                return true;
            }
            if (sender.equals(target)) {
                sender.sendMessage(ChatColor.RED + "You may not ignore yourself.");
                return true;
            }
            StaffPriority selfPriority = StaffPriority.of(player);
            if (StaffPriority.of(target).isMoreThan(selfPriority)) {
                sender.sendMessage(ChatColor.RED + "You cannot ignore this player.");
                return true;
            }
            if (target.hasPermission("command.ignore.exempt")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to ignore this player.");
                return true;
            }
            String targetName = target.getName();
            if (ignoring.add(target.getName())) {
                sender.sendMessage(ChatColor.GOLD + "You are now ignoring " + targetName + '.');
            }
            else {
                sender.sendMessage(ChatColor.RED + "You are already ignoring someone named " + targetName + '.');
            }
            return true;
        }
        
        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            return (args.length == 2) ? null : Collections.emptyList();
        }
    }
}
