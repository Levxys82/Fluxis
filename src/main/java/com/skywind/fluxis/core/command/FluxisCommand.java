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
            sender.sendMessage(core.message("errors.players_only", "{prefix}&cThis command is for players only!"));
            return true;
        }

        String commandName = command.getName();

        if (commandName.equalsIgnoreCase("market") || commandName.equalsIgnoreCase("shop")) {
            if (!player.hasPermission("fluxis.command.market")) {
                player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                return true;
            }
            core.getMarketGUI().open(player);
            return true;
        }

        if (commandName.equalsIgnoreCase("sell")) {
            if (!player.hasPermission("fluxis.command.sell")) {
                player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                return true;
            }
            core.getSellGUI().open(player);
            return true;
        }

        if (commandName.equalsIgnoreCase("auction")) {
            if (!player.hasPermission("fluxis.command.auction")) {
                player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                return true;
            }
            if (args.length >= 2 && args[0].equalsIgnoreCase("sell")) {
                if (!player.hasPermission("fluxis.auction.sell")) {
                    player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                    return true;
                }
                try {
                    double price = Double.parseDouble(args[1]);
                    if (price <= 0) {
                        player.sendMessage(core.message("errors.auction_price_positive", "{prefix}&cPrice must be greater than 0."));
                        return true;
                    }
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand == null || hand.getType() == Material.AIR) {
                        player.sendMessage(core.message("errors.hold_item_required", "{prefix}&cYou must hold an item in your hand!"));
                        return true;
                    }
                    
                    player.getInventory().setItemInMainHand(null);
                    core.getAuctionModule().getAuctionManager().listAction(
                        player.getUniqueId(), player.getName(), hand, price
                    );
                    String msg = core.message("info.item_listed", "{prefix}&aItem listed in Auction House for {amount} {currency}");
                    player.sendMessage(msg
                        .replace("{amount}", String.format("%.2f", price))
                        .replace("{currency}", core.getEconomyConfig().getString("currency.name", "Money")));
                } catch (NumberFormatException e) {
                    player.sendMessage(core.message("usage.auction_sell", "{prefix}&cUsage: /auction sell <price>"));
                }
                return true;
            }
            core.getAuctionModule().getAuctionGUI().open(player, 0);
            return true;
        }

        if (commandName.equalsIgnoreCase("trade")) {
            if (!player.hasPermission("fluxis.command.trade")) {
                player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                return true;
            }
            if (args.length < 1) {
                player.sendMessage(core.message("usage.trade", "{prefix}&cUsage: /trade <player>"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(core.message("errors.player_not_found", "{prefix}&cPlayer not found!"));
                return true;
            }
            if (target.equals(player)) {
                player.sendMessage(core.message("errors.trade_self", "{prefix}&cYou cannot trade with yourself!"));
                return true;
            }
            core.getTradeModule().getTradeManager().acceptRequest(player, target);
            return true;
        }

        if (commandName.equalsIgnoreCase("fluxis")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("balance")) {
                    double bal = core.getEconomyManager().getBalance(player.getUniqueId());
                    String msg = core.message("info.balance", "{prefix}&eYour Balance: &f{amount} {currency}");
                    player.sendMessage(msg
                        .replace("{amount}", String.format("%.2f", bal))
                        .replace("{currency}", core.getEconomyConfig().getString("currency.name", "Money")));
                    return true;
                }

                if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("fluxis.admin")) {
                        core.reloadAllConfigs();
                        player.sendMessage(core.message("info.config_reloaded", "{prefix}&aAll Fluxis configs reloaded!"));
                    } else {
                        player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                    }
                    return true;
                }
                
                if (args[0].equalsIgnoreCase("menu")) {
                    if (player.hasPermission("fluxis.admin")) {
                        core.getAdminGUI().open(player);
                    } else {
                        player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("event")) {
                    if (player.hasPermission("fluxis.admin")) {
                        core.getEventGUI().open(player);
                    } else {
                        player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                    }
                    return true;
                }
            }
            
            player.sendMessage(core.message("help.header", "&eFluxis Ecosystem &7- Modular Economy"));
            player.sendMessage(core.message("help.shop", "&7/shop - Open the dynamic shop"));
            player.sendMessage(core.message("help.sell", "&7/sell - Bulk sell items"));
            player.sendMessage(core.message("help.balance", "&7/fluxis balance - Check your money"));
            if (player.hasPermission("fluxis.admin")) {
                player.sendMessage(core.message("help.menu", "&c/fluxis menu - Admin Control Panel"));
                player.sendMessage(core.message("help.reload", "&c/fluxis reload - Reload all configs"));
                player.sendMessage(core.message("help.event", "&c/fluxis event - Open event panel"));
            }
        }

        return true;
    }
}
