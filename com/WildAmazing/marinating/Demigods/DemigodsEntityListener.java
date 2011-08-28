package com.WildAmazing.marinating.Demigods;

import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import OtherCommands.PhantomCommands;

import com.WildAmazing.marinating.Demigods.Demigods;
import com.WildAmazing.marinating.Demigods.Gods.Listeners.ApolloCommands;
import com.WildAmazing.marinating.Demigods.Gods.Listeners.AresCommands;
import com.WildAmazing.marinating.Demigods.Gods.Listeners.AthenaCommands;
import com.WildAmazing.marinating.Demigods.Gods.Listeners.GodCommands;
import com.WildAmazing.marinating.Demigods.Gods.Listeners.HadesCommands;
import com.WildAmazing.marinating.Demigods.Gods.Listeners.HephaestusCommands;
import com.WildAmazing.marinating.Demigods.Gods.Listeners.PoseidonCommands;
import com.WildAmazing.marinating.Demigods.Gods.Listeners.ZeusCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.CronusCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.HyperionCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.OceanusCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.PrometheusCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.StyxCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.TitanCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.TyphonCommands;
import com.WildAmazing.marinating.Demigods.Utilities.Cuboid;
import com.WildAmazing.marinating.Demigods.Utilities.DeityLocale;

public class DemigodsEntityListener extends EntityListener {
		private Demigods plugin;
	 
		public DemigodsEntityListener(Demigods instance) {
	        plugin = instance;
	    }
		public void onEntityDamage(EntityDamageEvent e){
			if (!plugin.getConfigHandler().isParticipating(e.getEntity().getWorld()))
				return;
			if (e.isCancelled())
				return;
			//overall
			GodCommands.onEntityDamage(e, plugin);
			TitanCommands.onEntityDamage(e, plugin);
			PhantomCommands.onPhantomDamage(e, plugin);
			//gods
			ZeusCommands.onEntityDamage(e, plugin);
			PoseidonCommands.onEntityDamage(e, plugin);
			HadesCommands.onEntityDamage(e, plugin);
			AresCommands.onEntityDamage(e, plugin);
			AthenaCommands.onEntityDamage(e, plugin);
			HephaestusCommands.onEntityDamage(e, plugin);
			ApolloCommands.onEntityDamage(e, plugin);
			//titans
			CronusCommands.onEntityDamage(e, plugin);
			PrometheusCommands.onEntityDamage(e, plugin);
			HyperionCommands.onEntityDamage(e, plugin);
			TyphonCommands.onEntityDamage(e, plugin);
			OceanusCommands.onEntityDamage(e, plugin);
			StyxCommands.onEntityDamage(e, plugin);
		}
		public void onEntityDeath(EntityDeathEvent e){
			if (!plugin.getConfigHandler().isParticipating(e.getEntity().getWorld()))
				return;
			//overall
			GodCommands.onEntityDeath(e, plugin);
			TitanCommands.onEntityDeath(e, plugin);
			//gods
			//titans
			CronusCommands.onEntityDeath(e, plugin);
			TyphonCommands.onEntityDeath(e, plugin);
			//other
			PhantomCommands.onPhantomDeath(e, plugin);
		}
		public void onEntityTarget(EntityTargetEvent e){
			if (!plugin.getConfigHandler().isParticipating(e.getEntity().getWorld()))
				return;
			if (e.isCancelled())
				return;
			PhantomCommands.onEntityTarget(e, plugin);
			//gods
			HadesCommands.onEntityTarget(e, plugin);
			AthenaCommands.onEntityTarget(e, plugin);
			//titans
		}
		public void onEntityCombust(EntityCombustEvent e){
			if (!plugin.getConfigHandler().isParticipating(e.getEntity().getWorld()))
				return;
			if (e.isCancelled())
				return;
			//gods
			HadesCommands.onEntityCombust(e, plugin);
			//titans
		}
		public void onExplosionPrime(ExplosionPrimeEvent e){
			if (!plugin.getConfigHandler().isParticipating(e.getEntity().getWorld()))
				return;
			if (e.isCancelled())
				return;
			for (DeityLocale dl : plugin.getAllLocs()){
				for (Cuboid c : dl.getLocale()){
					if (c.isInCuboid(e.getEntity().getLocation()))
						e.setRadius(0);
				}
			}
		}
}	