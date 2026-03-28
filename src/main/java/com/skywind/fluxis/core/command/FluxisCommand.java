package com.skywind.fluxis.core.command;

import com.skywind.fluxis.core.Fluxis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FluxisCommand implements CommandExecutor {

    private final Fluxis core;

    public FluxisCommand(Fluxis core) {
        this.core = core;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is for players only!");
            return true;
        }

        if (label.equalsIgnoreCase("market") || label.equalsIgnoreCase("shop")) {
            core.getMarketGUI().open(player);
            return true;
        }

        if (label.equalsIgnoreCase("sell")) {
            core.getSellGUI().open(player);
            return true;
        }

        if (label.equalsIgnoreCase("auction")) {
            if (args.length >= 2 && args[0].equalsIgnoreCase("sell")) {
                try {
                    double price = Double.parseDouble(args[1]);
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand == null || hand.getType() == Material.AIR) {
                        player.sendMessage("§cYou must hold an item in your hand!");
                        return true;
                    }
                    
                    player.getInventory().setItemInMainHand(null);
                    core.getAuctionModule().getAuctionManager().listAction(
                        player.getUniqueId(), player.getName(), hand, price
                    );
                    player.sendMessage("§aItem listed in Auction House for " + String.format("%.2f", price) + " Money");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cUsage: /auction sell <price>");
                }
                return true;
            }
            core.getAuctionModule().getAuctionGUI().open(player, 0);
            return true;
        }

        if (label.equalsIgnoreCase("trade")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /trade <player>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§cPlayer not found!");
                return true;
            }
            if (target.equals(player)) {
                player.sendMessage("§cYou cannot trade with yourself!");
                return true;
            }
            core.getTradeModule().getTradeManager().acceptRequest(player, target);
            return true;
        }

        if (label.equalsIgnoreCase("fluxis")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("balance")) {
                    double bal = core.getEconomyManager().getBalance(player.getUniqueId());
                    player.sendMessage("§eYour Balance: §f" + String.format("%.2f", bal) + " Money");
                    return true;
                }
                
                if (args[0].equalsIgnoreCase("menu")) {
                    if (player.hasPermission("fluxis.admin")) {
                        core.getAdminGUI().open(player);
                    } else {
                        player.sendMessage("§cYou don't have permission to use the admin panel!");
                    }
                    return true;
                }
            }
            
            player.sendMessage("§eFluxis Ecosystem §7- Modular Economy");
            player.sendMessage("§7/shop - Open the dynamic shop");
            player.sendMessage("§7/sell - Bulk sell items");
            player.sendMessage("§7/fluxis balance - Check your money");
            if (player.hasPermission("fluxis.admin")) {
                player.sendMessage("§c/fluxis menu - Admin Control Panel");
            }
        }

        return true;
    }
}
