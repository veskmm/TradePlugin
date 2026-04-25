package me.vesk.trade;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ManagerTrade {
    private final JavaPlugin plugin;

    public ManagerTrade(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Будем исказать заявки только по тому кто пригласил
    private Map<UUID, InviteTrade> activeInvites = new LinkedHashMap<>();
    private Map<Integer, InviteTrade> activeTrades = new LinkedHashMap<>();

    public boolean createInvite(Player inviter, Player target) {
        if (hasActiveInvite(inviter,target)) return false;
        InviteTrade invite = new InviteTrade(inviter, target);
        activeInvites.put(inviter.getUniqueId(), invite);
        return true;
    }

    public UUID getTarget(int num) {
        return activeTrades.get(num).getTarget();
    }
    public UUID getInvinter(int num) {
        return activeTrades.get(num).getInviter();
    }

    private int NextIndex = 0;

    public void startTrade(Player inviter, Player target) {
        activeTrades.put(NextIndex,activeInvites.get(inviter.getUniqueId()));
        activeInvites.remove(inviter.getUniqueId());

        MenuTrade menuTrade = new MenuTrade(inviter, target, this, NextIndex);
        MenuTrade menuTradeTwo = new MenuTrade(target, inviter, this, NextIndex);

        plugin.getServer().getPluginManager().registerEvents(menuTrade, plugin);
        plugin.getServer().getPluginManager().registerEvents(menuTradeTwo, plugin);

        activeTrades.get(NextIndex).setTargetMenu(menuTradeTwo);
        activeTrades.get(NextIndex).setInviterMenu(menuTrade);

        menuTrade.openMenu();
        menuTradeTwo.openMenu();

        NextIndex++;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public boolean isTradeActive(int tradeId) {
        return activeTrades.containsKey(tradeId);
    }

    public void addItemsToListInviter(ItemStack item, int num, Vector2i slot) {
        InviteTrade trade = activeTrades.get(num);
        if (trade != null && item != null) {
            trade.addItemToListInviter(slot, item.clone());
        }
    }
    public void removeItemsFromListInviter(Vector2i slot, int num) {
        InviteTrade trade = activeTrades.get(num);
        if (trade != null) {
            trade.removeItemFromListInviter(slot);
        }
    }

    public void addItemsToListTarget(ItemStack item, int num, Vector2i slot) {
        InviteTrade trade = activeTrades.get(num);
        if (trade != null && item != null) {
            trade.addItemToListTarget(slot, item.clone());
        }
    }
    public void removeItemsFromListTarget(Vector2i slot, int num) {
        InviteTrade trade = activeTrades.get(num);
        if (trade != null) {
            trade.removeItemFromListTarget(slot);
        }
    }

    public Map<Vector2i, ItemStack> getItemsFromListTarget(int num) {
        return activeTrades.get(num).getItemsListTarget();
    }
    public Map<Vector2i, ItemStack> getItemsFromListInviter(int num) {
        return activeTrades.get(num).getItemsListInviter();
    }

    public void tradeMenusUpdate(int num) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            InviteTrade trade = activeTrades.get(num);
            if (trade == null) {
                return;
            }

            trade.getInviterMenu().update();
            trade.getTargetMenu().update();
        });
    }

    private void addItemsToPlayerInventory(Player player, Map<Vector2i, ItemStack> itemsList) {
        if (player == null) return;
        if (itemsList == null || itemsList.isEmpty()) return;

        int itemsProcessed = 0;
        boolean droppedSomething = false;

        for (ItemStack item : itemsList.values()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            ItemStack itemToGive = item.clone();

            HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(itemToGive);

            if (!remaining.isEmpty()) {
                droppedSomething = true;

                for (ItemStack dropItem : remaining.values()) {
                    if (dropItem != null && dropItem.getType() != Material.AIR) {
                        player.getWorld().dropItemNaturally(player.getLocation(), dropItem);
                    }
                }
            }

            itemsProcessed++;
        }

        if (itemsProcessed > 0) {
            player.sendMessage(ChatColor.GREEN + "Получено предметов: " + itemsProcessed);
        }
        if (droppedSomething) {
            player.sendMessage(ChatColor.YELLOW + "Ваш инвентарь заполнен! Некоторые предметы выпали на землю.");
        }
    }

    private void unregisterTradeMenus(InviteTrade trade) {
        if (trade.getInviterMenu() != null) {
            HandlerList.unregisterAll(trade.getInviterMenu());
        }

        if (trade.getTargetMenu() != null) {
            HandlerList.unregisterAll(trade.getTargetMenu());
        }
    }

    public void completeTrade(int tradeId) {
        InviteTrade trade = activeTrades.get(tradeId);
        if (trade == null) return;

        Player inviter = Bukkit.getPlayer(trade.getInviter());
        Player target = Bukkit.getPlayer(trade.getTarget());

        if (inviter == null || target == null) {
            if (inviter != null) inviter.sendMessage(ChatColor.RED + "Торговля отменена: игрок вышел!");
            if (target != null) target.sendMessage(ChatColor.RED + "Торговля отменена: игрок вышел!");
            cancelTrade(tradeId);
            return;
        }

        // Обмен предметами
        addItemsToPlayerInventory(target, trade.getItemsListInviter());
        addItemsToPlayerInventory(inviter, trade.getItemsListTarget());

        // Сообщения
        inviter.sendMessage(ChatColor.GREEN + "Сделка успешно совершена!");
        target.sendMessage(ChatColor.GREEN + "Сделка успешно совершена!");

        // Закрываем меню
        unregisterTradeMenus(trade);
        activeTrades.remove(tradeId);

        inviter.closeInventory();
        target.closeInventory();
    }

    public void cancelTrade(int tradeId) {
        InviteTrade trade = activeTrades.get(tradeId);

        if (trade == null) {
            return;
        }

        activeTrades.remove(tradeId);

        unregisterTradeMenus(trade);

        Player inviter = Bukkit.getPlayer(trade.getInviter());
        Player target = Bukkit.getPlayer(trade.getTarget());

        if (inviter != null) {
            addItemsToPlayerInventory(inviter, trade.getItemsListInviter());
        }
        if (target != null) {
            addItemsToPlayerInventory(target, trade.getItemsListTarget());
        }

        if (inviter != null) {
            inviter.sendMessage(ChatColor.RED + "Торговля отменена.");
        }
        if (target != null) {
            target.sendMessage(ChatColor.RED + "Торговля отменена.");
        }

        if (inviter != null) {
            inviter.closeInventory();
        }
        if (target != null) {
            target.closeInventory();
        }
    }

    public void resetStateTrade(int num) {
        InviteTrade trade = activeTrades.get(num);
        if (trade == null) {return;}

        activeTrades.get(num).setIsTargetAccepted(InviteTrade.OfferState.PENDING);
        activeTrades.get(num).getInviterMenu().updateTradeStatusDisplay(InviteTrade.OfferState.PENDING);

        activeTrades.get(num).setIsInviterAccepted(InviteTrade.OfferState.PENDING);
        activeTrades.get(num).getTargetMenu().updateTradeStatusDisplay(InviteTrade.OfferState.PENDING);
    }

    public void acceptTrade(Player player, Player partner, int num) {
        if (player.getUniqueId().equals(activeTrades.get(num).getTarget())) {
            activeTrades.get(num).setIsTargetAccepted(InviteTrade.OfferState.ACCEPTED);
            activeTrades.get(num).getInviterMenu().updateTradeStatusDisplay(InviteTrade.OfferState.ACCEPTED);
        } else {
            activeTrades.get(num).setIsInviterAccepted(InviteTrade.OfferState.ACCEPTED);
            activeTrades.get(num).getTargetMenu().updateTradeStatusDisplay(InviteTrade.OfferState.ACCEPTED);
        }
        if (activeTrades.get(num).getIsInviterAccepted() == (activeTrades.get(num).getIsTargetAccepted()) &&
                activeTrades.get(num).getIsTargetAccepted() == (InviteTrade.OfferState.ACCEPTED)) {
            completeTrade(num);
        }

    }


    public boolean hasActiveInvite(Player inviter, Player target) {
        InviteTrade invite = activeInvites.get(inviter.getUniqueId());
        if (invite == null) return false;
        if (invite.getTarget().equals(target.getUniqueId())) {
            return true;
        }
        return false;
    }

}
