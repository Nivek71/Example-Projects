package nivek71.api.utility.input;

import nivek71.api.utility.Helpers;
import nivek71.api.utility.Logger;
import nivek71.api.utility.UtilityPlugin;
import nivek71.api.utility.functions.FunctionEx;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InventoryGUI {
    private static final Map<Inventory, InventoryGUI> inventoryGuiMap = new HashMap<>();
    private final Map<Loc, ClickResponse> clickResponseMap = new HashMap<>();
    private final List<Inventory> inventoryList = new ArrayList<>();
    private boolean dispose = false;

    public static void load() {
        Helpers.registerDynamicEvent(UtilityPlugin.getPlugin(), InventoryClickEvent.class, event -> {
            InventoryGUI gui = getOwningGUI(event.getClickedInventory());
            if (gui != null) {
                Loc loc = gui.new Loc(event.getClickedInventory(), event.getSlot());
                if (!gui.getResponse(loc).respond(loc, event))
                    event.setCancelled(true);
            }
        });

        Helpers.registerDynamicEvent(UtilityPlugin.getPlugin(), InventoryCloseEvent.class, event -> {
            InventoryGUI gui = getOwningGUI(event.getInventory());
            if (gui != null && gui.dispose)
                gui.dispose();
        }, EventPriority.MONITOR, true);
    }

    public InventoryGUI() {
    }

    public InventoryGUI(String guiName, int inventoryAmount, int inventorySize) {
        for (int i = 0; i < inventoryAmount; i++)
            getNewInventory(guiName, inventorySize);
    }

    public InventoryGUI(String guiName, int inventorySize) {
        this(guiName, 1, inventorySize);
    }

    public static InventoryGUI getOwningGUI(Inventory inventory) {
        return inventoryGuiMap.get(inventory);
    }

    // todo - add support for page items, which may be inserted automatically

    public static <T> InventoryGUI createGUI(String guiName, int pageSize, Collection<T> collection, FunctionEx<T, ItemStack> itemManager, FunctionEx<T, ClickResponse> responseManager) {
        return new InventoryGUI(guiName, collection.size() / pageSize + (collection.size() % pageSize == 0 ? 0 : 1), pageSize).addCollection(collection, itemManager, responseManager);
    }

    public <T> InventoryGUI addCollection(Collection<T> collection, int inventoryIndex, int slotIndex, FunctionEx<T, ItemStack> itemManager, FunctionEx<T, ClickResponse> responseManager) {
        Validate.isTrue(getPageCount() > inventoryIndex, "page count out of bounds");
        Validate.isTrue(inventoryList.get(inventoryIndex).getSize() > slotIndex, "slot index out of bounds");
        for (T element : collection) {
            ItemStack item = Logger.fetchOrLog(() -> itemManager.applyEx(element));
            ClickResponse response = Logger.fetchOrLog(() -> responseManager.applyEx(element));
            if (item == null || response == null) continue;

            Loc loc = getLoc(inventoryIndex, slotIndex);
            setItem(loc, item);
            if (response != getDefaultResponse())
                setResponse(loc, response);

            if (++slotIndex == loc.getInventory().getSize()) {
                Validate.isTrue(getPageCount() > ++inventoryIndex, "page out of bounds");
                slotIndex = 0;
            }
        }
        return this;
    }

    public <T> InventoryGUI addCollection(Collection<T> collection, FunctionEx<T, ItemStack> itemManager, FunctionEx<T, ClickResponse> responseManager) {
        return addCollection(collection, 0, 0, itemManager, responseManager);
    }

    public <T> InventoryGUI addCollection(Collection<T> collection, FunctionEx<T, ItemStack> itemManager) {
        return addCollection(collection, 0, 0, itemManager, element -> getDefaultResponse());
    }

    protected void unregisterInventory(Inventory inventory) {
        if (inventoryList.remove(inventory)) {
            clickResponseMap.entrySet().removeIf(entry -> entry.getKey().inventory.equals(inventory));
            inventoryGuiMap.remove(inventory);
        }
    }

    protected void unregisterInventories() {
        for (int i = inventoryList.size() - 1; i >= 0; i--)
            inventoryGuiMap.remove(inventoryList.remove(i));
    }

    private Inventory registerInventory(Inventory inventory) {
        inventoryGuiMap.put(inventory, this);
        inventoryList.add(inventory);
        return inventory;
    }

    public final Inventory getNewInventory(InventoryType type, String title) {
        return registerInventory(Bukkit.createInventory(null, type, title));
    }

    public final Inventory getNewInventory(String title, int size) {
        return registerInventory(Bukkit.createInventory(null, size, title));
    }

    public int getPageCount() {
        return inventoryList.size();
    }

    public Inventory getPageAt(int index) {
        return inventoryList.get(index);
    }

    public void showPlayer(Player player, int page) {
        Validate.validIndex(inventoryList, page, "Inventory page out of bounds");
        player.openInventory(inventoryList.get(page));
    }

    public void showPlayer(Player player) {
        if (getPageCount() == 0)
            player.sendMessage(ChatColor.RED + "No entries to display");
        else showPlayer(player, 0);
    }

    public void disposeAfter(Player player) {
        dispose = true;
        showPlayer(player);
    }

    // todo - if player's inventory is forcibly closed due to teleportation, re-open (and don't dispose)

    public void dispose() {
        unregisterInventories();
    }

    public void setItem(Loc loc, ItemStack item) {
        loc.getInventory().setItem(loc.slotIndex, item);
    }

    public void setResponse(Loc loc, ClickResponse response) {
        if (response == null)
            clickResponseMap.remove(loc);
        else clickResponseMap.put(loc, response);
    }

    public void setSlot(Loc loc, ItemStack item, ClickResponse response) {
        setItem(loc, item);
        setResponse(loc, response);
    }

    public ClickResponse getDefaultResponse() {
        return ClickResponse.NO_RESPONSE;
    }

    public ClickResponse getResponse(Loc loc) {
        return clickResponseMap.getOrDefault(loc, getDefaultResponse());
    }

    public Loc getLoc(int inventoryIndex, int slotIndex) {
        Validate.validIndex(inventoryList, inventoryIndex, "inventoryIndex out of bounds");
        return new Loc(inventoryList.get(inventoryIndex), slotIndex);
    }

    public class Loc {
        private final Inventory inventory;
        private final int slotIndex;

        private Loc(Inventory inventory, int slotIndex) {
            this.inventory = inventory;
            this.slotIndex = slotIndex;
        }

        public InventoryGUI getGUI() {
            return InventoryGUI.this;
        }

        public Inventory getInventory() {
            return inventory;
        }

        public int getSlotIndex() {
            return slotIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Loc loc = (Loc) o;
            return slotIndex == loc.slotIndex &&
                    inventory.equals(loc.inventory);
        }

        @Override
        public int hashCode() {
            return Objects.hash(inventory, slotIndex);
        }
    }
}
