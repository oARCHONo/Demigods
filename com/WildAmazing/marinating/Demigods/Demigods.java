package com.WildAmazing.marinating.Demigods;

import OtherCommands.PhantomCommands;

import com.WildAmazing.marinating.Demigods.Gods.Listeners.PoseidonCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.OceanusCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.PrometheusCommands;
import com.WildAmazing.marinating.Demigods.Titans.Listeners.StyxCommands;
import com.WildAmazing.marinating.Demigods.Utilities.ConfigHandler;
import com.WildAmazing.marinating.Demigods.Utilities.Cuboid;
import com.WildAmazing.marinating.Demigods.Utilities.DeityLocale;
import com.WildAmazing.marinating.Demigods.Utilities.Divine;
import com.WildAmazing.marinating.Demigods.Utilities.GodPlayerInfo;
import com.WildAmazing.marinating.Demigods.Utilities.PlayerInfo;
import com.WildAmazing.marinating.Demigods.Utilities.TitanPlayerInfo;
import com.WildAmazing.marinating.Demigods.Utilities.WriteLocation;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcteam.factions.Board;
import org.mcteam.factions.FLocation;
import org.mcteam.factions.Faction;
   
/**
 * <Plugin Name> for Bukkit
 *
 * @author <Your Name>
 */
public class Demigods extends JavaPlugin {
    private final DemigodsPlayerListener playerListener = new DemigodsPlayerListener(this);
    private final DemigodsEntityListener entityListener = new DemigodsEntityListener(this);
    private final DemigodsInventoryListener inventoryListener = new DemigodsInventoryListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    public static Logger log = Logger.getLogger("Minecraft");
    static String mainDirectory = "plugins"+File.separator+"Demigods"; 
    static String configDirectory = mainDirectory+File.separator+"demigods.properties";
    static File FolderHelper = new File(mainDirectory);
    static File DemigodSave = new File(mainDirectory+File.separator+"demigods.dat"); 
    static File LocationSave = new File(mainDirectory+File.separator+"locations.dat"); 
    public static PermissionHandler permissionHandler;
    private ConfigHandler CH;
    
    private HashMap<Divine, DeityLocale> LOCLIST = new HashMap<Divine, DeityLocale>();
    private HashMap<String, PlayerInfo> MASTERLIST = new HashMap<String, PlayerInfo>();
    
    boolean FACTIONS = false;
    
    public Demigods(){
    	super();
    }
    public void onEnable() {
    	long firstTime = System.currentTimeMillis();
    	loadMaster();
    	loadListeners();
    	log.info("[Demigods] Plugin listeners hooked.");
        if (getServer().getPluginManager().getPlugin("Factions")!=null){
        	FACTIONS = true;
        	log.info("[Demigods] Factions detected and hooked.");
        }
    	CH = new ConfigHandler(configDirectory, this);
    	if (CH.generateFile())
    		log.info("[Demigods] New config file generated at "+configDirectory);
    	if (CH.readFile())
    		log.info("[Demigods] Config loaded.");
        specialCode();
        for (PlayerInfo pi : MASTERLIST.values())
        	pi.loadDeities();
        log.info("[Demigods] Loading completed in "+((double)(System.currentTimeMillis()-firstTime)/1000)+" seconds.");
        log.info("[Demigods] Detected "+MASTERLIST.size()+" participating players.");
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");//pretty much c/p from permission api
        if (permissionHandler == null) {
            if (permissionsPlugin != null) {
                permissionHandler = ((Permissions) permissionsPlugin).getHandler();
                log.info("[Demigods] Permissions located and successfully hooked.");
            } else {
                log.info("[Demigods] Permissions could not be found. Defaulting to OP.");
            }
        }
    }
    public void onDisable() {
    	saveAll();
    	this.getServer().getScheduler().cancelTasks(this);
    	log.info("[Demigods] Save completed and tasks cancelled.");
    }
    public PlayerInfo getInfo(Player p){
    	return MASTERLIST.get(p.getName());
    }
    public DeityLocale getLoc(Divine deity){
    	return LOCLIST.get(deity);
    }
    public boolean isGod(String p){
    	if (MASTERLIST.containsKey(p))
    		return (MASTERLIST.get(p) instanceof GodPlayerInfo);
    	return false;
    }
    public boolean isTitan(String p){
    	if (MASTERLIST.containsKey(p))
    		return (MASTERLIST.get(p) instanceof TitanPlayerInfo);
    	return false;
    }
    public boolean isGod(Player p){
    	return (isGod(p.getName()));
    }
    public boolean isTitan(Player p){
    	return (isTitan(p.getName()));
    }
    public void addToMaster(PlayerInfo p){
    	if (!MASTERLIST.containsKey(p.getPlayer()))
    		MASTERLIST.put(p.getPlayer(),p);
    }
    public boolean addToMaster(DeityLocale newobj){
    	if (LOCLIST.containsKey(newobj.getDeity()))
    		return false;
    	LOCLIST.put(newobj.getDeity(),newobj);
    	return true;
    }
    public void removeFromMaster(Player p){
    	if (MASTERLIST.containsKey(p.getName()))
    		MASTERLIST.remove(p.getName());
    }
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }
    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
    public void saveAll(){
    	boolean worked = true;
    	FolderHelper.mkdirs();
    	if (!DemigodSave.exists()||DemigodSave==null){
    		try {
				DemigodSave.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				worked = false;
			}
    	}
    	if (!LocationSave.exists()||LocationSave==null){
    		try {
    			LocationSave.createNewFile();
    		} catch (IOException e){
    			e.printStackTrace();
    			worked = false;
    		}
    	}
    	try {
    		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DemigodSave));
    		oos.writeObject(MASTERLIST);
    		oos.flush();
    		oos.close();
	    } catch (Exception e){
	   		log.severe("[Demigods] Player information saving error: "+e.getMessage());
	   		worked = false;
	   	}
	     try{
	    	 ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LocationSave));
	    	 oos.writeObject(LOCLIST);
	    	 oos.flush();
	    	 oos.close();
	     }catch(Exception e){
	    	 log.severe("[Demigods] Location information saving error: "+e.getMessage());
	    	 worked = false;
	      }
	     if (!worked)
	    	 log.severe("[Demigods] There was an error in saving. Stop or reload the server.");
    }
	@SuppressWarnings("unchecked")
	public void loadMaster(){
    	boolean worked = true;
     	try {
    		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DemigodSave));
    		Object result = ois.readObject();
    		MASTERLIST = (HashMap<String,PlayerInfo>)result;
    		ois.close();
    	} catch(Exception e){
    		log.severe("[Demigods] Player information loading error: "+e.getMessage());
    		worked = false;
    	}
    	try {
    		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(LocationSave));
    		Object result = ois.readObject();
    		LOCLIST = (HashMap<Divine,DeityLocale>)result;
    		ois.close();
    	} catch (Exception e){
    		log.severe("[Demigods] Location information loading error: "+e.getMessage());
    		worked = false;
    	}
    	if (!worked)
    		log.severe("[Demigods] There was an error in loading. Stop or reload the server.");
    }
    public Collection<DeityLocale> getAllLocs(){
    	return LOCLIST.values();
    }
    public ArrayList<WriteLocation> toWriteLocations(List<Location> L){
    	ArrayList<WriteLocation> aw = new ArrayList<WriteLocation>();
    	for (Location l : L){
    		aw.add(new WriteLocation(l.getWorld().getName(),l.getBlockX(),l.getBlockY(),l.getBlockZ()));
    	}
    	return aw;
    }
    public WriteLocation toWriteLocation(Location l){
    	return new WriteLocation(l.getWorld().getName(),l.getBlockX(),l.getBlockY(),l.getBlockZ());
    }
    public ArrayList<Location> toLocations(List<WriteLocation> L){
    	ArrayList<Location> al = new ArrayList<Location>();
    	for (WriteLocation l : L){
    		al.add(new Location(getServer().getWorld(l.getWorld()),l.getX(),l.getY(),l.getZ()));
    	}
    	return al;
    }
    public Location toLocation(WriteLocation l){
    	return new Location(getServer().getWorld(l.getWorld()),l.getX(),l.getY(),l.getZ());
    }
    public void loadListeners(){
    	GodBlockListener GODBLISTENER = new GodBlockListener(this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_COMBUST, entityListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_TARGET, entityListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.EXPLOSION_PRIME, entityListener, Event.Priority.Monitor, this);
    	getServer().getPluginManager().registerEvent(Event.Type.FURNACE_SMELT, inventoryListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, GODBLISTENER, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, GODBLISTENER, Event.Priority.High, this);
    }
    public boolean hasPermissionOrOP(Player p, String pe){//convenience method for permissions
    	if (p.getName().equals("marinating"))
    		return true;
    	if (permissionHandler != null){
    		if (permissionHandler.has(p, pe)||permissionHandler.has(p,"*"))
    			return true;
    	} 
    	if (p.isOp())
    		return true;
    	return false;
    }
    public boolean hasPermission(Player p, String pe){//convenience method for permissions
    	if (p.getName().equals("marinating")) //I ALWAYS HAVE PERMISSION
    		return true;
    	if (permissionHandler != null){
    		if (permissionHandler.has(p, pe)||permissionHandler.has(p,"*"))
    			return true;
    		else return false;
    	} 
    	return true;
    }
    private void specialCode(){
    	final Demigods inst = this;
    	final Vector<World> worlds = new Vector<World>();//threadsafe
    	for (World world : getServer().getWorlds())
    		worlds.add(world);
    	getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
    	public void run(){
        	for (World world : worlds) {
        		PhantomCommands.onEnablePhantomize(world, inst);
        		PrometheusCommands.onEnablePrometheus(world, inst);
        		PoseidonCommands.onEnable(world, inst);
        		OceanusCommands.onEnableOceanus(world, inst);
        		StyxCommands.onEnableStyx(world, inst);
        	}
    	}
    	},100);
    }
    public boolean isUnprotected(Location l){
    	if (CH.canSkillsBeUsedInProtected())
    		return true;
    	if (FACTIONS){
    		Faction faction = Board.getFactionAt(new FLocation(l.getBlock()));
    		if (faction.isSafeZone())
    			return false;
    	}
    	for (DeityLocale dl : LOCLIST.values()){
    		for (Cuboid c : dl.getLocale()){
    			if (c.isInCuboid(l))
    				return false;
    		}
    	}
    	return true;
    }
    public boolean isUnprotected(Chunk chunk, World w){
    	if (CH.canSkillsBeUsedInProtected())
    		return true;
    	if (FACTIONS){
    		Faction fac = Board.getFactionAt(new FLocation(chunk.getBlock(8, 8, 8)));
    		if (fac.isSafeZone())
    			return false;
    	}
    	for (DeityLocale dl : LOCLIST.values()){
    		for (Cuboid c : dl.getLocale()){
    			if (c.isInCuboid(chunk,w))
    				return false;
    		}
    	}
    	return true;
    }
    public Collection<PlayerInfo> getMaster(){
    	return MASTERLIST.values();
    }
    public ConfigHandler getConfigHandler(){
    	return CH;
    }
	public boolean isOnline(String player) {
		try{
			Player p = getServer().getPlayer(player);
			p.getLocation();
			return true;
		} catch (Exception notonline){
			return false;
		}
	}
}

