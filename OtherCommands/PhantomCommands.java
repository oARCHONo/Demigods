package OtherCommands;

/*
 * Thanks to EvilNilla on Bukkit for the vanish code.
 */
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.WildAmazing.marinating.Demigods.Demigods;
import com.WildAmazing.marinating.Demigods.Utilities.Cuboid;
import com.WildAmazing.marinating.Demigods.Utilities.DeityLocale;
import com.WildAmazing.marinating.Demigods.Utilities.Divine;

public class PhantomCommands {
	public static void onEnablePhantomize(final World w, final Demigods plugin){
		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable(){
			public void run(){
				if (w==null)
					return;
				for (Player p : w.getPlayers()){
					if (p.isOnline())
					if ((plugin.isTitan(p)||plugin.isGod(p)))
						if (!plugin.getInfo(p).isAlive()){
							phantomize(p, plugin);
						}
				}
			}
		}, 0, 20);
	}
	public static void onPlayerJoinPhantom(PlayerJoinEvent e, Demigods plugin){
		Player pl = e.getPlayer();
		if (!(plugin.isTitan(pl)||plugin.isGod(pl)))
			return;
		if (plugin.getInfo(pl).isAlive())
			return;
		pl.sendMessage(ChatColor.GRAY+"You are a phantom.");
	}
	public static void onPlayerRespawnPhantom(PlayerRespawnEvent e, final Demigods plugin){
		final Player p = e.getPlayer();
		if (!(plugin.isTitan(p)||plugin.isGod(p)))
			return;
		if (plugin.getInfo(p).isAlive())
			return;
		phantomize(p, plugin);
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
			public void run(){
				p.teleport(plugin.toLocation(plugin.getInfo(p).getLastLoc()));
				p.sendMessage(ChatColor.GRAY+"Your immortal soul has been released.");
				p.sendMessage(ChatColor.GRAY+"Return to a shrine of one of your deities to ");
				p.sendMessage(ChatColor.GRAY+"resurrect yourself. Left click the ground to get");
				p.sendMessage(ChatColor.GRAY+"your bearings.");
			}
		},10);
	}
	public static void onPhantomInteract(PlayerInteractEvent e, Demigods plugin){
		Player p = e.getPlayer();
		if (!(plugin.isTitan(p)||plugin.isGod(p)))
			return;
		if (plugin.getInfo(p).isAlive())
			return;
		if (e.getClickedBlock()!=null){
			for (DeityLocale d : plugin.getAllLocs()){
				if (plugin.getInfo(p).hasDeity(d.getDeity())){
					for (Cuboid c : d.getLocale()){
						if (c.isInCuboid(e.getClickedBlock().getLocation()) && c.isShrine()){
							plugin.getInfo(p).setAlive(true);
							unphantomize(p);
							p.getWorld().strikeLightningEffect(p.getLocation());
							for (Player pl : p.getWorld().getPlayers()){
								pl.sendMessage(ChatColor.GOLD+p.getName()+" has been resurrected to the mortal world.");
							}
							return;
						}
					}
				}
			}
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK){
				if (plugin.hasPermission(p, "phantom.teleport")){
					Block b = e.getClickedBlock();
					float pitch = p.getLocation().getPitch();
					float yaw = p.getLocation().getYaw();
					Location newloc = new Location(p.getWorld(),(double)b.getX(),(double)p.getWorld().getHighestBlockYAt(b.getX(), b.getZ()),(double)b.getZ());
					newloc.setPitch(pitch);
					newloc.setYaw(yaw);
					p.teleport(newloc);
					return;
				}
			} else if (e.getAction() == Action.LEFT_CLICK_BLOCK){
				int nearest = Integer.MAX_VALUE;
				String point = "";
				for (DeityLocale d : plugin.getAllLocs()){
					if (plugin.getInfo(p).hasDeity(d.getDeity())){
						for (Cuboid c : d.getLocale()){
							if (c.isShrine()){
								Location loc1 = plugin.toLocation(c.getCorner1());
								Location loc2 = plugin.toLocation(c.getCorner2());
								Location shrineloc = (loc1.toVector().getMidpoint(loc2.toVector())).toLocation(p.getWorld());
								if ((int)shrineloc.toVector().distance(p.getLocation().toVector())<nearest){
									point = "";
									if (shrineloc.getBlockX()>p.getLocation().getBlockX())
										point += 'S';
									else if (shrineloc.getBlockX()<p.getLocation().getBlockX())
										point += 'N';
									if (shrineloc.getBlockZ()>p.getLocation().getBlockZ())
										point += 'W';
									else if (shrineloc.getBlockZ()<p.getLocation().getBlockZ())
										point += 'E';
									nearest = (int)plugin.toLocation(c.getCorner1()).toVector().distance(p.getLocation().toVector());
								}
							}
						}
					}
				}
				if (point.equals("")){
					plugin.getInfo(p).setAlive(true);
					unphantomize(p);
					p.getWorld().strikeLightningEffect(p.getLocation());
					for (Player pl : p.getWorld().getPlayers()){
						pl.sendMessage(ChatColor.GOLD+p.getName()+" has been resurrected to the mortal world.");
					}
					return;
				}
				if (nearest != Integer.MAX_VALUE) {
					p.sendMessage(ChatColor.GRAY+"The nearest shrine is "+nearest+" blocks away ("+point+").");
					return;
				}
				else {
					p.sendMessage(ChatColor.GRAY+"No shrines found. You will be resurrected automatically.");
					plugin.getInfo(p).setAlive(true);
					return;
				}
			}
		}
		p.sendMessage(ChatColor.GRAY+"Phantoms cannot interact with the mortal world.");
		e.setCancelled(true);
	}
	public static void onEntityTarget(EntityTargetEvent e, Demigods plugin){
		if (e.getTarget() instanceof Player){
			Player p = (Player)e.getTarget();
			if (!(plugin.isTitan(p)||plugin.isGod(p)))
				return;
			if (plugin.getInfo(p).isAlive())
				return;
			e.setCancelled(true);
		}
	}
	public static void onPhantomItemPickup(PlayerPickupItemEvent e, Demigods plugin){
		if (e.isCancelled())
			return;
		Player p = e.getPlayer();
		if (!(plugin.isTitan(p)||plugin.isGod(p)))
			return;
		if (plugin.getInfo(p).isAlive())
			return;
		e.setCancelled(true);
	}
	public static void onPhantomItemDrop(PlayerDropItemEvent e, Demigods plugin){
		Player p = e.getPlayer();
		if (!(plugin.isTitan(p)||plugin.isGod(p)))
			return;
		if (plugin.getInfo(p).isAlive())
			return;
		p.sendMessage(ChatColor.GRAY+"Phantoms cannot interact with the mortal world.");
		e.setCancelled(true);
	}
	public static void onPhantomDamage(EntityDamageEvent e, Demigods plugin){
		try {
			EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent)e;
			if (ee.getDamager() instanceof Player){
				if (plugin.isTitan((Player)ee.getDamager())||plugin.isGod((Player)ee.getDamager())){
					if (!plugin.getInfo((Player)ee.getDamager()).isAlive()){
						e.setDamage(0);
						e.setCancelled(true);
						return;
					}
				}
			}
		} catch (Exception no){}
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player)e.getEntity();
		if (!(plugin.isTitan(p)||plugin.isGod(p)))
			return;
		if (plugin.getInfo(p).isAlive())
			return;
		e.setDamage(0);
		e.setCancelled(true);
	}
	public static void onPhantomBlockBreak(BlockBreakEvent e, Demigods plugin){
		Player p = e.getPlayer();
		if (!(plugin.isTitan(p)||plugin.isGod(p)))
			return;
		if (plugin.getInfo(p).isAlive())
			return;
		p.sendMessage(ChatColor.GRAY+"Phantoms cannot interact with the mortal world.");
		e.setCancelled(true);
	}
	public static void onPhantomBlockPlace(BlockPlaceEvent e, Demigods plugin){
		Player p = e.getPlayer();
		if (!(plugin.isTitan(p)||plugin.isGod(p)))
			return;
		if (plugin.getInfo(p).isAlive())
			return;
		p.sendMessage(ChatColor.GRAY+"Phantoms cannot interact with the mortal world.");
		e.setCancelled(true);
	}
	public static void phantomize(Player p, Demigods plugin){
		CraftPlayer hide = (CraftPlayer)p;
		for (Player pl : p.getWorld().getPlayers()){
			if (!pl.equals(p)){
				CraftPlayer hideFrom = (CraftPlayer)pl;
				hideFrom.getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(hide.getHandle()));
				hideFrom.getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(hide.getEntityId()));
			}		
			if (plugin.isGod(pl)){
			if (plugin.getInfo(pl).hasDeity(Divine.HADES)) {
				if ((pl.getInventory().getArmorContents()[3].getType() == Material.GOLD_HELMET))
					if (!pl.equals(p)){
						CraftPlayer unHideFrom = (CraftPlayer)pl;
						unHideFrom.getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(hide.getEntityId()));
						unHideFrom.getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(hide.getHandle()));
					}
			} 
			} else if (plugin.isTitan(pl)){
			if (plugin.getInfo(pl).hasDeity(Divine.STYX))
				if (!pl.equals(p)){
					CraftPlayer unHideFrom = (CraftPlayer)pl;
					unHideFrom.getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(hide.getEntityId()));
					unHideFrom.getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(hide.getHandle()));
				}
			}
		}
	}
	public static void unphantomize(Player p){
		CraftPlayer unHide = (CraftPlayer)p;
		for (Player pl : p.getWorld().getPlayers()){
			if (!pl.equals(p)){
				CraftPlayer unHideFrom = (CraftPlayer)pl;
				unHideFrom.getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(unHide.getEntityId()));
				unHideFrom.getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(unHide.getHandle()));
			}
		}
	}
	public static void onPhantomChat(PlayerChatEvent e, Demigods plugin){
		Player p = e.getPlayer();
		if (!(plugin.isTitan(p)||plugin.isGod(p)))
			return;
		if (plugin.getInfo(p).isAlive())
			return;
		e.setMessage(ChatColor.GRAY+"<PHANTOM> "+e.getMessage());
	}
	public static void onPhantomDeath(EntityDeathEvent e, Demigods plugin){
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player)e.getEntity();
		if (!(plugin.isTitan(p)||plugin.isGod(p)))
			return;
		if (plugin.getInfo(p).isAlive())
			return;
		phantomize(p, plugin);
		p.setHealth(20);
	}
}
