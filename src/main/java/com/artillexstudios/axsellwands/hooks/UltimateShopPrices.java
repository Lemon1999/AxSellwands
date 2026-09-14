package com.artillexstudios.axsellwands.hooks;

import com.artillexstudios.axintegrations.types.ShopIntegration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class UltimateShopPrices extends ShopIntegration {

    private Method getVaultBuyPrice;
    private Method getVaultSellPrice;

    public UltimateShopPrices() {
        super("UltimateShop");
    }

    @Override
    public boolean canLoad() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("UltimateShop");
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public boolean setup() {
        try {
            Class<?> shopHelperClass = Class.forName("cn.superiormc.ultimateshop.api.ShopHelper");
            getVaultBuyPrice = shopHelperClass.getMethod("getVaultBuyPrice", ItemStack[].class, Player.class, int.class);
            getVaultSellPrice = shopHelperClass.getMethod("getVaultSellPrice", ItemStack[].class, Player.class, int.class);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            Bukkit.getLogger().warning("[AxSellwands] Failed to hook into UltimateShop: " + exception.getMessage());
            return false;
        }
    }

    @Override
    public @Nullable Double getBuyPrice(@NonNull ItemStack itemStack) {
        return null;
    }

    @Override
    public @Nullable Double getBuyPrice(UUID uuid, @NonNull ItemStack itemStack) {
        Player player = Bukkit.getPlayer(uuid);
        return getPrice(getVaultBuyPrice, player, itemStack);
    }

    @Override
    public @Nullable Double getSellPrice(@NonNull ItemStack itemStack) {
        return null;
    }

    @Override
    public @Nullable Double getSellPrice(UUID uuid, @NonNull ItemStack itemStack) {
        Player player = Bukkit.getPlayer(uuid);
        return getPrice(getVaultSellPrice, player, itemStack);
    }

    private @Nullable Double getPrice(@Nullable Method method, @Nullable Player player, @NonNull ItemStack itemStack) {
        if (method == null || player == null || !player.isOnline() || itemStack.getType().isAir()) {
            return null;
        }

        int amount = itemStack.getAmount();
        if (amount <= 0) {
            return null;
        }

        try {
            double price = (double) method.invoke(null, new ItemStack[]{itemStack.clone()}, player, amount);
            if (price <= 0) {
                return null;
            }
            return price;
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException exception) {
            Bukkit.getLogger().warning("[AxSellwands] Failed to read UltimateShop price for " + itemStack.getType().name() + ": " + exception.getMessage());
            return null;
        }
    }
}
