package com.WildAmazing.marinating.Demigods;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryListener;

import com.WildAmazing.marinating.Demigods.Gods.Listeners.HephaestusCommands;

public class DemigodsInventoryListener extends InventoryListener {
	private Demigods plugin;
	public DemigodsInventoryListener(Demigods instance) {
        plugin = instance;
    }
	public void onFurnaceSmelt(FurnaceSmeltEvent e){
		HephaestusCommands.onFurnaceSmelt(e, plugin);
	}
}