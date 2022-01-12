package main.conquer.Listeners;

import main.conquer.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionListener {

    /*
    TODO MAJOR CONDITIONALS!
     */

    //TODO Save hashmaps to a config on disable, and load them from the config onenable. (collection memory is wiped on server end)
    //TODO Test not having collections just config

    // HashMap for each faction created upon creation ->
    // key = factionName, value is members in order of insertion, (1 will always == the leader.)
    private final HashMap<String, LinkedHashSet<String>> faction;
    //Array of each faction for internal use
    private final ArrayList<HashMap<String, LinkedHashSet<String>>> list;

    //TODO Have each player that is invited to have a unique countdown. [Faction: [Player:Countdown]]


    public FactionListener() {
        this.faction = new HashMap<>();
        list = new ArrayList<>();
    }

    //Save arraylist in members, and just add, make sure to always get array
    //TODO boolean checks if invited for the future
    //TODO Check if player isn't in a Faction
    public void joinFaction(Faction faction, Player player, String factionName) {
            if (Main.getMain().getCustomConfig().getConfigurationSection(factionName) != null) {
                if (getFactionOfPlayer(player).equalsIgnoreCase("null")) {
                    setMembers(player, factionName, true);
                    sendEventMessage(faction, player, factionName);
                } else player.sendMessage(ChatColor.RED + "Whoops, you're already a member of a Faction!");
            }

    }

    //TODO Fix leaving faction again
    public void leaveFaction(Faction faction, Player player) {
        if(getMembers(getFactionOfPlayer(player)).contains(player.getName())) {
            player.sendMessage(faction.getMessage() + " " + getFactionOfPlayer(player));
            setMembers(player, false);
        }else player.sendMessage("err");
        //update config
        //TODO Put new data in faction hashmap
    }

    //TODO Message loop through each player in faction, give a message
    public void disbandFaction(Faction faction, Player player) {
        if (player.getName().equals(getFactionLeader(player))) {
            String getFaction = getFactionOfPlayer(player);

            Main.getMain().getCustomConfig().set(getFaction, null);

            getMembers(getFaction).clear();
            this.faction.get(getFaction).clear();
            list.remove(getFaction);
            //Test of members loop
            for (String players : getMembers(getFactionOfPlayer(player))) {
                if (players.equalsIgnoreCase(player.getName())) {
                    continue;
                } else {
                    sendEventMessage(faction, Bukkit.getPlayer(players));
                }
            }
            saveConfig();
        }
    }

    public void createFaction(Faction faction, Player player, String factionName) {
        if (!getFactionOfPlayer(player).equalsIgnoreCase("null")) return;
        if (Main.getMain().getCustomConfig().getConfigurationSection(factionName) == null) {
            LinkedHashSet<String> members = new LinkedHashSet<>();
            this.faction.put(factionName, members);

            list.add(this.faction);

            createConfigSection(factionName);
            addConfigSectionChildren(factionName, "leader.name", player.getName());
            addConfigSectionChildren(factionName, "members", members.toArray());
            saveConfig();
            //TODO Default children
        } else {
            player.sendMessage(ChatColor.RED + "" + faction + " already exists!");
        }

    }


    public synchronized void createConfigSection(String sectionNameParent) {
        if (Main.getMain().getCustomConfig().getConfigurationSection(sectionNameParent) == null) {
            Main.getMain().getCustomConfig().createSection(sectionNameParent);
            saveConfig();
        }

    }

    public synchronized void addConfigSectionChildren(String sectionNameParent, String path, Object value) {
        //TODO Conditionals if !exist
        if (Main.getMain().getCustomConfig().getConfigurationSection(sectionNameParent) != null) {
            Objects.requireNonNull(Main.getMain().getCustomConfig().getConfigurationSection(sectionNameParent)).set(path, value);
            saveConfig();
        }

    }

    public void saveConfig() {
        try {
            Main.getMain().save();
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            Bukkit.getServer().getConsoleSender().sendMessage("Config successfully saved.");
        }

    }


    public LinkedHashSet<String> getMembers(String factionName) {

        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        if(Main.getMain().getCustomConfig().getConfigurationSection(factionName) != null) {
            if (Main.getMain().getCustomConfig().getConfigurationSection(factionName).getStringList("members").size() > 0) {
                for (String members : Main.getMain().getCustomConfig().getConfigurationSection(factionName).getStringList("members")) {
                    hashSet.add(members.toString());
                    return hashSet;
                }
            }
        }else return null;
        return hashSet;



    }

    public boolean factionExists(String factionName){
        return Main.getMain().getCustomConfig().getConfigurationSection(factionName) != null;
    }

    //TODO Fix getting faction from a player member
    public String getFactionOfPlayer(Player player) {
        for (String keys : Main.getMain().getCustomConfig().getConfigurationSection("").getKeys(false)) {
            if (Main.getMain().getCustomConfig().getConfigurationSection(keys).get("leader.name").equals(player.getName()) ||
            Main.getMain().getCustomConfig().getConfigurationSection(keys).getStringList("members").contains(player.getName())) {
                return keys;
            }

            for(String members : Main.getMain().getCustomConfig().getConfigurationSection(keys).getStringList("members")){
                    if(members.equalsIgnoreCase(player.getName())) {
                        return keys;
                    }
                }

        }
        return "null";
    }

    public String getFactionLeader(String faction) {
        return (String) Main.getMain().getCustomConfig().getConfigurationSection(faction).get("leader.name");

    }

    public String getFactionLeader(Player player) {
        String factionOfPlayer = getFactionOfPlayer(player);
        return (String) Main.getMain().getCustomConfig().getConfigurationSection(factionOfPlayer).get("leader.name");

    }

    public void setMembers(Player player, boolean addOrRemove) {
        if (addOrRemove) {
            LinkedHashSet<String> members = getMembers(getFactionOfPlayer(player));
            members.add(player.getName());
            this.faction.put(getFactionOfPlayer(player), members);
            Main.getMain().getCustomConfig().getConfigurationSection(getFactionOfPlayer(player)).set("members", members.toArray());
            saveConfig();
        } else {
            LinkedHashSet<String> members = getMembers(getFactionOfPlayer(player));
            members.remove(player.getName());
            this.faction.put(getFactionOfPlayer(player), members);
            Main.getMain().getCustomConfig().getConfigurationSection(getFactionOfPlayer(player)).set("members", members.toArray());
            saveConfig();
        }

    }


    public void setMembers(Player player, String factionName, boolean addOrRemove) {
        if (addOrRemove) {
            LinkedHashSet<String> members = getMembers(factionName);
            members.add(player.getName());
            this.faction.put(factionName, members);
            Main.getMain().getCustomConfig().getConfigurationSection(factionName).set("members", members.toArray());
            saveConfig();
        } else {
            LinkedHashSet<String> members = getMembers(factionName);
            members.remove(player.getName());
            this.faction.put(factionName, members);
            Main.getMain().getCustomConfig().getConfigurationSection(factionName).set("members", members.toArray());
            saveConfig();
        }

    }

    public void sendEventMessage(Faction faction, Player player) {
        switch (faction) {
            case CREATE, LEAVE, JOIN, DISBAND, PROMOTE, MENU -> player.sendMessage(faction.getMessage());
        }

    }

    public void sendEventMessage(Faction faction, Player player, String extraMessage) {
        switch (faction) {
            case CREATE, LEAVE, JOIN, DISBAND, PROMOTE, MENU -> player.sendMessage(faction.getMessage() + extraMessage);
        }
    }


}