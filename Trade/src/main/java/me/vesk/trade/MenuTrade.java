package me.vesk.trade;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.joml.Vector2i;

import java.util.LinkedHashMap;
import java.util.Map;


public class MenuTrade implements Listener {

    private final Player player;
    private final Player partner;
    private final ChestGui gui;
    private final ManagerTrade managerTrade;
    private final int NumberTrade;

    private StaticPane partnerPane;
    private StaticPane playerPane;

    private StaticPane StateItem;

    private InviteTrade.OfferState OfferStateState = InviteTrade.OfferState.PENDING;

    public MenuTrade(Player player, Player partner, ManagerTrade managerTrade, int numberTrade) {
        this.player = player;
        this.partner = partner;
        this.gui = new ChestGui(6, "Торговля с " + partner.getName());
        this.managerTrade = managerTrade;
        this.NumberTrade = numberTrade;

        setup();
    }

    @EventHandler
    private void onClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("Торговля с " + partner.getName())) {
            return;
        }

        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            return;
        }

        Bukkit.getScheduler().runTask(managerTrade.getPlugin(), () -> {
            if (!managerTrade.isTradeActive(NumberTrade)) {
                return;
            }

            if (!player.isOnline()) {
                managerTrade.cancelTrade(NumberTrade);
                return;
            }

            String currentTitle = player.getOpenInventory().getTitle();
            if (currentTitle.equals("Торговля с " + partner.getName())) {
                return;
            }
            managerTrade.cancelTrade(NumberTrade);
        });
    }

    private void setup() {
        // ============ Задний Фон ============
        StaticPane background = new StaticPane(0, 0, 9, 6);

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);

        for (int x = 0; x <= 8; x++) {
            background.addItem(new GuiItem(filler, event -> {event.setCancelled(true);}), x, 0);
        }
        for (int x = 1; x <= 7; x++) {
            background.addItem(new GuiItem(filler, event -> {event.setCancelled(true);}), x, 5);
        }

        // ============ Линия разделения зон игроков ============
        ItemStack separator = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta sepMeta = separator.getItemMeta();
        sepMeta.setDisplayName(" ");
        separator.setItemMeta(sepMeta);

        StaticPane separatorLine = new StaticPane(4, 0, 1, 6);
        for (int y = 0; y < 6; y++) {
            separatorLine.addItem(new GuiItem(separator, event -> {event.setCancelled(true);}), 0, y);
        }
        gui.addPane(separatorLine);

        // ============ Функциональные кнопки ============
        StaticPane FuncItems = new StaticPane(0, 5, 1, 1);
        ItemStack buttonAccept = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta acceptMeta = buttonAccept.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "ПРИНЯТЬ ПРЕДЛОЖЕНИЕ");
        buttonAccept.setItemMeta(acceptMeta);
        FuncItems.addItem(new GuiItem(buttonAccept, event -> {
            onAcceptTrade();
            event.setCancelled(true);
        }), 0, 0);
        gui.addPane(FuncItems);

        // ============ Блок состояние принятия ============
        StateItem = new StaticPane(8,5,1,1);
        ItemStack blockStatic = new ItemStack(Material.LIGHT_GRAY_WOOL);
        ItemMeta staticMeta = blockStatic.getItemMeta();
        staticMeta.setDisplayName(ChatColor.GRAY + "ОЖИДАНИЕ");
        blockStatic.setItemMeta(staticMeta);
        StateItem.addItem(new GuiItem(blockStatic, event -> {
            event.setCancelled(true);
        }), 0,0);
        gui.addPane(StateItem);

        // ============ Меню предметов игроков ============
        partnerPane = new StaticPane(5, 1, 4, 4);
        playerPane = new StaticPane(0,1,4,4);
        gui.addPane(playerPane);
        gui.addPane(partnerPane);

        // ============ Добавляем фон (должен быть последним, чтобы не перекрывать другие элементы) ============
        gui.addPane(background);
    }

    public void update() {
        if (partnerPane != null) {
            partnerPane.clear();
        }
        if (playerPane != null) {
            playerPane.clear();
        }
        Map<Vector2i, ItemStack> items = new LinkedHashMap<>();
        if (player.getUniqueId().equals(managerTrade.getTarget(NumberTrade))) {
            items = managerTrade.getItemsFromListInviter(NumberTrade);
        } else {
            items = managerTrade.getItemsFromListTarget(NumberTrade);
        }
        for (Map.Entry<Vector2i, ItemStack> entry : items.entrySet()) {
            Vector2i pos = entry.getKey();
            ItemStack item = entry.getValue();

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            int localX = pos.x;
            int localY = pos.y - 1;

            if (localX >= 0 && localX < 4 && localY >= 0 && localY < 4) {
                ItemStack displayItem = item.clone();

                GuiItem guiItem = new GuiItem(displayItem, event -> {
                    event.setCancelled(true);
                });

                partnerPane.addItem(guiItem, localX, localY);
            } else {
            }
        }

        Map<Vector2i, ItemStack> itemsList = managerTrade.getItemsFromListTarget(NumberTrade);

        if (player.getUniqueId().equals(managerTrade.getInvinter(NumberTrade))) {
            itemsList = managerTrade.getItemsFromListInviter(NumberTrade);
        }

        for (Map.Entry<Vector2i, ItemStack> entry : itemsList.entrySet()) {

            Vector2i cord = entry.getKey();
            ItemStack item = entry.getValue();

            if (itemsList.get(cord) == null || itemsList.get(cord).getType().equals(Material.AIR)) {
                continue;
            }
            int localX = cord.x;
            int localY = cord.y - 1;

            if (localX >= 0 && localX < 4 && localY >= 0 && localY < 4) {
                playerPane.addItem(new GuiItem(itemsList.get(cord).clone()), localX, localY);
            }
        }

        StateItem.clear();
        ItemStack blockStatic = new ItemStack(Material.LIGHT_GRAY_WOOL);
        ItemMeta staticMeta = blockStatic.getItemMeta();
        if (OfferStateState.equals(InviteTrade.OfferState.ACCEPTED)) {
            blockStatic = new ItemStack(Material.GREEN_WOOL);
            staticMeta.setDisplayName(ChatColor.DARK_GREEN + "ПРИНЯТО");
        } else if (OfferStateState.equals(InviteTrade.OfferState.DECLINED)) {
            blockStatic = new ItemStack(Material.RED_WOOL);
            staticMeta.setDisplayName(ChatColor.DARK_RED + "ОТКАЗ");
        } else if (OfferStateState.equals(InviteTrade.OfferState.PENDING)) {
            blockStatic = new ItemStack(Material.LIGHT_GRAY_WOOL);
            staticMeta.setDisplayName(ChatColor.GRAY + "ОЖИДАНИЕ");
        }

        blockStatic.setItemMeta(staticMeta);
        StateItem.addItem(new GuiItem(blockStatic, event -> {
            event.setCancelled(true);
        }), 0,0);

        gui.update();
    }

    public void updateTradeStatusDisplay(InviteTrade.OfferState state) {
        this.OfferStateState = state;
        update();
    }


    @EventHandler
    private void slotClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Торговля с " + partner.getName())) {
            return;
        }

        if (event.getWhoClicked() != player) {
            return;
        }

        if (event.getClickedInventory() == null) {
            event.setCancelled(true);
            return;
        }

        InventoryAction action = event.getAction();

        if (event.getClickedInventory() instanceof PlayerInventory) {
            if (
                    event.isShiftClick()
                            || action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                            || action == InventoryAction.HOTBAR_SWAP
                            || action == InventoryAction.HOTBAR_MOVE_AND_READD
                            || action == InventoryAction.COLLECT_TO_CURSOR
            ) {
                event.setCancelled(true);
            }

            return;
        }

        event.setCancelled(true);

        ItemStack cursorItem = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();

        int eventSlot = event.getSlot();

        int row = eventSlot / 9;
        int col = eventSlot % 9;

        Vector2i blockSlot = new Vector2i(col, row);
        if (blockSlot.y < 1 || blockSlot.y > 4 || blockSlot.x >= 4) {
            return;
        }

        managerTrade.resetStateTrade(NumberTrade);

        // Обработка действий
        if (action == InventoryAction.PICKUP_ALL && currentItem != null && currentItem.getType() != Material.AIR) {
            if (managerTrade.getInvinter(NumberTrade).equals(player.getUniqueId())) {
                managerTrade.removeItemsFromListInviter(blockSlot, NumberTrade);
            } else {
                managerTrade.removeItemsFromListTarget(blockSlot, NumberTrade);
            }
            player.setItemOnCursor(currentItem.clone());
            event.setCurrentItem(null);
        }
        else if(action == InventoryAction.PLACE_ALL && cursorItem != null && cursorItem.getType() != Material.AIR) {
            if (currentItem != null && currentItem.getType() != Material.AIR) {
                return;
            }
            if (managerTrade.getInvinter(NumberTrade).equals(player.getUniqueId())) {
                managerTrade.addItemsToListInviter(cursorItem, NumberTrade, blockSlot);
            } else {
                managerTrade.addItemsToListTarget(cursorItem, NumberTrade, blockSlot);
            }
            event.setCurrentItem(cursorItem.clone());
            player.setItemOnCursor(null);
        }
        // Обновляем меню второго игрока
        managerTrade.tradeMenusUpdate(NumberTrade);
    }

    public void openMenu() {
        gui.show(player);
    }

    private void onAcceptTrade() {
        player.sendMessage("Вы приняли приглашение");
        managerTrade.acceptTrade(player, partner, NumberTrade);
    }
}