package me.austinlm.extravaganza;

import java.io.IOException;
import java.util.UUID;
import me.austinlm.extravaganza.image.ImageDisplay;
import me.austinlm.extravaganza.image.ImageManager;
import me.austinlm.extravaganza.listeners.PetListener;
import me.austinlm.extravaganza.npc.NPC;
import me.austinlm.extravaganza.npc.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtravaganzaPlugin extends JavaPlugin {

    private static ExtravaganzaPlugin instance;
    private NPCManager npcManager;
    private ImageManager imageManager;
    private PetListener petListener;

    public static ExtravaganzaPlugin getInstance() {
        return instance;
    }

    private static final UUID BIRTHDAY_UUID = UUIDCache.ANIMALTAMER1;
    public static boolean isBirthday(Entity entity) {
        return entity instanceof Player && entity.getUniqueId().equals(BIRTHDAY_UUID);
    }

    @Override
    public void onEnable() {
        instance = this;
        this.npcManager = new NPCManager();
        this.npcManager.init();
        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 42, 37 ,74, -90, 0), UUIDCache.DEANN, "MDeann", (p, n) -> {
            p.sendMessage("Happy Birthday Ani! Thank you for all the work that you do!");
            giveItem(p, Material.DIAMOND_PICKAXE);
        }));

        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 43.5, 40 ,66, 0, 20), UUIDCache.GROVE, "xGrove", (p, n) -> {
            p.sendMessage("Happy birthday hope you enjoy the texture pack!!");
            giveItem(p, Material.DIAMOND_AXE);
        }));

        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 40.5, 52 ,69.5, -43, 12), UUIDCache.REDNED, "Redned", (p, n) -> {
            p.sendMessage("Happy b-day Animal!! Thanks for all the amazing work you do for the team and all the hard work you put in <3");
            giveItem(p, Material.DIAMOND_SHOVEL);
        }));

        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 41.3, 52, 77.5, -134, 12), UUIDCache.CALICHIN, "calichin", (p, n) -> {
            p.sendMessage("Have a good birthday better admin!");
            giveItem(p, Material.DIAMOND_HOE);
        }));

        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 44.5, 64, 65.5, 0, 15), UUIDCache.ENDER, "EnderKnightError", (p, n) -> {
            p.sendMessage("Happy birthday Aminal! Thanks for all the hard work and effort you put in for the team <3");
            giveItem(p, Material.DIAMOND_SWORD);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                Zombie zombie = p.getWorld().spawn(p.getLocation(), Zombie.class);
                p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1f, 1f);
                Bukkit.getScheduler().runTaskLater(this, zombie::remove, 20);
            }, 10);
        }));

        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 48.5, 52, 74.5, -40, 47), UUIDCache.FINIXLY, "Finixly", (p, n) -> {
            p.sendMessage("Happy birthday Animal!!! Thanks for everyone you’ve done for us and the rest of the team <3 :)");
        }));

        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), -130.5, 187, 71.5, 0, 35), UUIDCache.ALM, "ALM", (p, n) -> {
            p.sendMessage("wait for iiiitttttttttt");
            Bukkit.getScheduler().runTaskLater(this, () -> {
                giveItem(p, Material.DIAMOND_HELMET);
            }, 40);
        }));

        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 49.5, 208, 222.5, 140, 5), UUIDCache.YOUN, "Younisco", (p, n) -> {
            p.sendMessage("Happy birthday Animal!");
        }));

        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 43.5, 208, 222.5, -135, 5), UUIDCache.WOUTO, "Wouto1997", (p, n) -> {
            p.sendMessage("I promise the song will come soon!!!");
        }));

        this.npcManager.registerNPC(new NPC(new Location(Bukkit.getWorlds().get(0), 43.5, 208, 215.5, -35, 5), UUIDCache.PINEAPPLE, "xPineapplee", (p, n) -> {
            p.sendMessage("Aaaahhhhhhhh, I can't think of anything to say, therefore I will say nothing at all. Actually wait... Happy birthday Animal!!");
        }));

        try {
            this.imageManager = new ImageManager(
                new ImageDisplay("https://i.imgur.com/bZAjPcc.png", new Location(Bukkit.getWorlds().get(0), 46.5, 215 ,224.5), 0.05)
            );
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getServer().shutdown();
            return;
        }

        Bukkit.getPluginManager().registerEvents(this.npcManager, this);
        Bukkit.getPluginManager().registerEvents(this.imageManager, this);
        Bukkit.getPluginManager().registerEvents(this.petListener = new PetListener(EntityType.OCELOT, "Skye"), this);
    }

    @Override
    public void onDisable() {
        if (this.petListener != null) this.petListener.disable();
        if (this.imageManager != null) this.imageManager.despawn();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!checkPerms(sender)) {
            return false;
        }
        return command.getName().equalsIgnoreCase("bday");
    }

    private boolean checkPerms(CommandSender sender) {
        if (sender instanceof BlockCommandSender) {
            return false;
        }
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }
        if (sender instanceof Player) {
            return sender.isOp();
        }
        return false;
    }

    private void giveItem(Player player, Material material) {
        Bukkit.getScheduler().runTaskLater(this, () ->
                player.getInventory().addItem(new ItemStack(material)), 40);
    }
}
