package me.vesk.trade;

import java.util.LinkedHashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2i;

import java.util.Map;

public class InviteTrade {

    // =============== Игроки ===============
    private UUID inviter;
    private UUID target;

    // =============== Время создания ===============
    private long createAt;

    // =============== Менюшки ===============
    private MenuTrade targetMenu;
    private MenuTrade inviterMenu;

    // =============== Списки с предметами игроков ===============
    private Map<Vector2i, ItemStack> itemsListInviter = new LinkedHashMap<>();
    private Map<Vector2i, ItemStack> itemsListTarget = new LinkedHashMap<>();

    // =============== Enum с возможными состояниями принятия сделки ===============
    public enum OfferState {
        ACCEPTED,
        DECLINED,
        PENDING
    }

    // =============== Состояния принятия сделки ===============
    private OfferState isInviterAccepted = OfferState.PENDING;
    private OfferState isTargetAccepted = OfferState.PENDING;


    public OfferState getIsInviterAccepted() {
        return isInviterAccepted;
    }

    public OfferState getIsTargetAccepted() {
        return isTargetAccepted;
    }

    public void setIsTargetAccepted(OfferState isTargetAccepted) {
        this.isTargetAccepted = isTargetAccepted;
    }

    public void setIsInviterAccepted(OfferState isInviterAccepted) {
        this.isInviterAccepted = isInviterAccepted;
    }

    // =============== Базовый ===============
    public InviteTrade(Player inviter, Player target) {
        this.inviter = inviter.getUniqueId();
        this.target = target.getUniqueId();
        this.createAt = System.currentTimeMillis();
    }

    // =============== Менюшки ===============
    public void setTargetMenu(MenuTrade targetMenu) {
        this.targetMenu = targetMenu;
    }
    public void setInviterMenu(MenuTrade inviterMenu) {
        this.inviterMenu = inviterMenu;
    }

    public MenuTrade getTargetMenu() {
        return targetMenu;
    }
    public MenuTrade getInviterMenu() {
        return inviterMenu;
    }

    // =============== Игроки ===============
    public UUID getInviter() {return inviter;}
    public UUID getTarget() {return target;}

    // =============== Время создания ===============
    public long getCreateAt() {return createAt;}

    // =============== Массивы с предметами ===============
    public Map<Vector2i, ItemStack> getItemsListInviter() {return itemsListInviter;}
    public Map<Vector2i, ItemStack> getItemsListTarget() {return itemsListTarget;}


    // =============== Добавлени/Удаление предметов ===============
    public void addItemToListInviter(Vector2i slot, ItemStack item) {
        itemsListInviter.put(slot, item);
    }
    public void addItemToListTarget(Vector2i slot, ItemStack item) {
        itemsListTarget.put(slot, item);
    }

    public void removeItemFromListInviter(Vector2i slot) {
        itemsListInviter.remove(slot);
    }
    public void removeItemFromListTarget(Vector2i slot) {
        itemsListTarget.remove(slot);
    }
}
