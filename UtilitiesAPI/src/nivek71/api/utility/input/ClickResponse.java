package nivek71.api.utility.input;

import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface ClickResponse {
    ClickResponse NO_RESPONSE = (loc, event) -> false;

    /**
     * @param loc the clicked location
     * @param event the event triggering this response
     * @return false to prevent item from moving, true to allow item to move
     */
    boolean respond(InventoryGUI.Loc loc, InventoryClickEvent event);
}
