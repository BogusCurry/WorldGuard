// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author sk89q
 */
public class BlacklistEntry {
    /**
     * Used to prevent spamming.
     */
    private static Map<String,BlacklistTrackedEvent> lastAffected =
            new HashMap<String,BlacklistTrackedEvent>();
    /**
     * Parent blacklist entry.
     */
    private Blacklist blacklist;
    /**
     * List of groups to not affect.
     */
    private Set<String> ignoreGroups;
    /**
     * List of actions to perform on destruction.
     */
    private String[] destroyActions;
    /**
     * List of actions to perform on break.
     */
    private String[] breakActions;
    /**
     * List of actions to perform on left click.
     */
    private String[] destroyWithActions;
    /**
     * List of actions to perform on block placement.
     */
    private String[] placeActions;
    /**
     * List of actions to perform on item use.
     */
    private String[] useActions;
    /**
     * List of actions to perform on right click upon.
     */
    private String[] rightClickActions;
    /**
     * List of actions to perform on drop.
     */
    private String[] dropActions;
    /**
     * List of actions to perform on drop.
     */
    private String[] acquireActions;
    /**
     * Message for users.
     */
    private String message;
    /**
     * Comment for administrators.
     */
    private String comment;

    /**
     * Used to ignore messages.
     */
    private static ActionHandler silentHandler = new ActionHandler() {
            public void log(String itemName) {}
            public void kick(String itemName) {}
            public void ban(String itemName) {}
            public void notifyAdmins(String itemName) {}
            public void tell(String itemName) {}
        };

    /**
     * Construct the object.
     * 
     * @param blacklist
     */
    public BlacklistEntry(Blacklist blacklist) {
        this.blacklist = blacklist;
    }

    /**
     * @return the ignoreGroups
     */
    public String[] getIgnoreGroups() {
        return ignoreGroups.toArray(new String[ignoreGroups.size()]);
    }

    /**
     * @param ignoreGroups the ignoreGroups to set
     */
    public void setIgnoreGroups(String[] ignoreGroups) {
        Set<String> ignoreGroupsSet = new HashSet<String>();
        for (String group : ignoreGroups) {
            ignoreGroupsSet.add(group.toLowerCase());
        }
        this.ignoreGroups = ignoreGroupsSet;
    }

    /**
     * @return
     */
    public String[] getDestroyActions() {
        return destroyActions;
    }

    /**
     * @param actions
     */
    public void setDestroyActions(String[] actions) {
        this.destroyActions = actions;
    }

    /**
     * @return
     */
    public String[] getBreakActions() {
        return breakActions;
    }

    /**
     * @param actions
     */
    public void setBreakActions(String[] actions) {
        this.breakActions = actions;
    }

    /**
     * @return
     */
    public String[] getDestroyWithActions() {
        return destroyWithActions;
    }

    /**
     * @param action
     */
    public void setDestroyWithActions(String[] actions) {
        this.destroyWithActions = actions;
    }

    /**
     * @return
     */
    public String[] getPlaceActions() {
        return placeActions;
    }

    /**
     * @param actions
     */
    public void setPlaceActions(String[] actions) {
        this.placeActions = actions;
    }

    /**
     * @return
     */
    public String[] getUseActions() {
        return useActions;
    }

    /**
     * @param actions
     */
    public void setUseActions(String[] actions) {
        this.useActions = actions;
    }

    /**
     * @return
     */
    public String[] getRightClickActions() {
        return rightClickActions;
    }

    /**
     * @param actions
     */
    public void setRightClickActions(String[] actions) {
        this.rightClickActions = actions;
    }

    /**
     * @return
     */
    public String[] getDropActions() {
        return dropActions;
    }

    /**
     * @param actions
     */
    public void setDropActions(String[] actions) {
        this.dropActions = actions;
    }

    /**
     * @return
     */
    public String[] getAcquireActions() {
        return acquireActions;
    }

    /**
     * @param actions
     */
    public void setAcquireActions(String[] actions) {
        this.acquireActions = actions;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns true if this player should be ignored.
     *
     * @param player
     * @return
     */
    public boolean shouldIgnore(Player player) {
        if (ignoreGroups == null) {
            return false;
        }
        for (String group : player.getGroups()) {
            if (ignoreGroups.contains(group.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Announces a message to all administrators.
     * 
     * @param str
     */
    public void notifyAdmins(String str) {
        for (Player player : etc.getServer().getPlayerList()) {
            if (player.canUseCommand("/wprotectalerts")
                    || player.canUseCommand("/worldguardnotify")) {
                player.sendMessage(Colors.LightGray + "WG: " + str);
            }
        }
    }

    /**
     * Ban a player.
     * 
     * @param player
     * @param msg
     */
    public void banPlayer(Player player, String msg) {
        etc.getServer().ban(player.getName());
        etc.getLoader().callHook(PluginLoader.Hook.BAN, new Object[]{
            player.getUser(), player.getUser(), msg
        });
        player.kick(msg);
    }

    /**
     * Called on block destruction. Returns true to let the action pass
     * through.
     *
     * @param block
     * @param player
     * @return
     */
    public boolean onDestroy(final Block block, final Player player) {
        if (destroyActions == null) {
            return true;
        }

        final BlacklistEntry entry = this;

        ActionHandler handler = new ActionHandler() {
            public void log(String itemName) {
                blacklist.getLogger().logDestroyAttempt(player, block, comment);
            }
            public void kick(String itemName) {
                player.kick("You are not allowed to destroy " + itemName);
            }
            public void ban(String itemName) {
                entry.banPlayer(player, "Banned: You are not allowed to destroy " + itemName);
            }
            public void notifyAdmins(String itemName) {
                entry.notifyAdmins(player.getName() + " (destroy) " + itemName
                        + (comment != null ? " (" + comment + ")" : "") + ".");
            }
            public void tell(String itemName) {
                player.sendMessage(Colors.Yellow + "You are not allowed to destroy " + itemName + ".");
            }
        };

        return process(block.getType(), player, destroyActions, handler, false, false);
    }

    /**
     * Called on block break. Returns true to let the action pass
     * through.
     *
     * @param block
     * @param player
     * @return
     */
    public boolean onBreak(final Block block, final Player player) {
        if (breakActions == null) {
            return true;
        }

        final BlacklistEntry entry = this;

        ActionHandler handler = new ActionHandler() {
            public void log(String itemName) {
                blacklist.getLogger().logBreakAttempt(player, block, comment);
            }
            public void kick(String itemName) {
                player.kick("You are not allowed to break " + itemName);
            }
            public void ban(String itemName) {
                entry.banPlayer(player, "Banned: You are not allowed to break " + itemName);
            }
            public void notifyAdmins(String itemName) {
                entry.notifyAdmins(player.getName() + " (break) " + itemName
                        + (comment != null ? " (" + comment + ")" : "") + ".");
            }
            public void tell(String itemName) {
                player.sendMessage(Colors.Yellow + "You are not allowed to break " + itemName + ".");
            }
        };

        return process(block.getType(), player, breakActions, handler, true, false);
    }

    /**
     * Called on left click. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onDestroyWith(final int item, final Player player) {
        if (destroyWithActions == null) {
            return true;
        }

        final BlacklistEntry entry = this;

        ActionHandler handler = new ActionHandler() {
            public void log(String itemName) {
                blacklist.getLogger().logDestroyWithAttempt(player, item, comment);
            }
            public void kick(String itemName) {
                player.kick("You can't destroy with " + itemName);
            }
            public void ban(String itemName) {
                entry.banPlayer(player, "Banned: You can't destroy with " + itemName);
            }
            public void notifyAdmins(String itemName) {
                entry.notifyAdmins(player.getName() + " (destroy w/) " + itemName
                        + (comment != null ? " (" + comment + ")" : "") + ".");
            }
            public void tell(String itemName) {
                player.sendMessage(Colors.Yellow + "You can't destroy with " + itemName + ".");
            }
        };

        return process(item, player, destroyWithActions, handler, false, false);
    }

    /**
     * Called on block placement. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onPlace(final int item, final Player player) {
        if (placeActions == null) {
            return true;
        }

        final BlacklistEntry entry = this;

        ActionHandler handler = new ActionHandler() {
            public void log(String itemName) {
                blacklist.getLogger().logPlaceAttempt(player, item, comment);
            }
            public void kick(String itemName) {
                player.kick("You can't place " + itemName);
            }
            public void ban(String itemName) {
                entry.banPlayer(player, "Banned: You can't place " + itemName);
            }
            public void notifyAdmins(String itemName) {
                entry.notifyAdmins(player.getName() + " (place) " + itemName
                        + (comment != null ? " (" + comment + ")" : "") + ".");
            }
            public void tell(String itemName) {
                player.sendMessage(Colors.Yellow + "You can't place " + itemName + ".");
            }
        };

        return process(item, player, placeActions, handler, true, false);
    }

    /**
     * Called on use. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onUse(final int item, final Player player) {
        if (useActions == null) {
            return true;
        }

        final BlacklistEntry entry = this;

        ActionHandler handler = new ActionHandler() {
            public void log(String itemName) {
                blacklist.getLogger().logUseAttempt(player, item, comment);
            }
            public void kick(String itemName) {
                player.kick("You can't use " + itemName);
            }
            public void ban(String itemName) {
                entry.banPlayer(player, "Banned: You can't use " + itemName);
            }
            public void notifyAdmins(String itemName) {
                entry.notifyAdmins(player.getName() + " (use) " + itemName
                        + (comment != null ? " (" + comment + ")" : "") + ".");
            }
            public void tell(String itemName) {
                player.sendMessage(Colors.Yellow + "You're not allowed to use " + itemName + ".");
            }
        };

        return process(item, player, useActions, handler, false, false);
    }

    /**
     * Called on right click upon. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onRightClick(final Block block, final Player player) {
        if (useActions == null) {
            return true;
        }

        final BlacklistEntry entry = this;

        ActionHandler handler = new ActionHandler() {
            public void log(String itemName) {
                blacklist.getLogger().logRightClickAttempt(player, block, comment);
            }
            public void kick(String itemName) {
                player.kick("You can't use " + itemName);
            }
            public void ban(String itemName) {
                entry.banPlayer(player, "Banned: You can't use " + itemName);
            }
            public void notifyAdmins(String itemName) {
                entry.notifyAdmins(player.getName() + " (use) " + itemName
                        + (comment != null ? " (" + comment + ")" : "") + ".");
            }
            public void tell(String itemName) {
                player.sendMessage(Colors.Yellow + "You're not allowed to use " + itemName + ".");
            }
        };

        return process(block.getType(), player, useActions, handler, false, false);
    }

    /**
     * Called on right click upon. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onSilentUse(final Block block, final Player player) {
        if (useActions == null) {
            return true;
        }

        return process(block.getType(), player, useActions, silentHandler, false, true);
    }

    /**
     * Called on item drop. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onDrop(final int item, final Player player) {
        if (dropActions == null) {
            return true;
        }

        final BlacklistEntry entry = this;

        ActionHandler handler = new ActionHandler() {
            public void log(String itemName) {
                blacklist.getLogger().logDropAttempt(player, item, comment);
            }
            public void kick(String itemName) {
                player.kick("You can't drop " + itemName);
            }
            public void ban(String itemName) {
                entry.banPlayer(player, "Banned: You can't drop " + itemName);
            }
            public void notifyAdmins(String itemName) {
                entry.notifyAdmins(player.getName() + " (drop) " + itemName
                        + (comment != null ? " (" + comment + ")" : "") + ".");
            }
            public void tell(String itemName) {
                player.sendMessage(Colors.Yellow + "You're not allowed to drop " + itemName + ".");
            }
        };

        return process(item, player, dropActions, handler, true, false);
    }

    /**
     * Called on item acquire. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onAcquire(final int item, final Player player) {
        if (acquireActions == null) {
            return true;
        }

        final BlacklistEntry entry = this;

        ActionHandler handler = new ActionHandler() {
            public void log(String itemName) {
                blacklist.getLogger().logAcquireAttempt(player, item, comment);
            }
            public void kick(String itemName) {
                player.kick("You can't acquire " + itemName);
            }
            public void ban(String itemName) {
                entry.banPlayer(player, "Banned: You can't acquire " + itemName);
            }
            public void notifyAdmins(String itemName) {
                entry.notifyAdmins(player.getName() + " (acquire) " + itemName
                        + (comment != null ? " (" + comment + ")" : "") + ".");
            }
            public void tell(String itemName) {
                player.sendMessage(Colors.Yellow + "You're not allowed to acquire " + itemName + ".");
            }
        };

        return process(item, player, acquireActions, handler, true, false);
    }

    /**
     * Called on item acquire. Returns true to let the action pass through.
     *
     * @param item
     * @param player
     * @return
     */
    public boolean onSilentAcquire(final int item, final Player player) {
        if (acquireActions == null) {
            return true;
        }

        return process(item, player, acquireActions, silentHandler, false, true);
    }

    /**
     * Internal method to handle the actions.
     * 
     * @param id
     * @param player
     * @param actions
     * @param handler
     * @param allowRepeat
     * @param silent
     * @return
     */
    private boolean process(int id, Player player, String[] actions,
            ActionHandler handler, boolean allowRepeat, boolean silent) {

        if (shouldIgnore(player)) {
            return true;
        }

        String name = player.getName();
        long now = System.currentTimeMillis();
        boolean repeating = false;

        // Check to see whether this event is being repeated
        BlacklistTrackedEvent tracked = lastAffected.get(name);
        if (tracked != null) {
            if (tracked.getId() == id && tracked.getTime() > now - 3000) {
                repeating = true;
            } else {
                tracked.setTime(now);
                tracked.setId(id);
            }
        } else {
            lastAffected.put(name, new BlacklistTrackedEvent(id, now));
        }

        boolean ret = true;
        
        for (String action : actions) {
            // Deny
            if (action.equalsIgnoreCase("deny")) {
                if (silent) {
                    return false;
                }
                ret = false;

            // Kick
            } else if (action.equalsIgnoreCase("kick")) {
                if (this.message != null) {
                    player.kick(String.format(this.message,
                            etc.getDataSource().getItem(id)));
                } else {
                    handler.kick(etc.getDataSource().getItem(id));
                }

            // Ban
            } else if (action.equalsIgnoreCase("ban")) {
                handler.ban(etc.getDataSource().getItem(id));
                if (this.message != null) {
                    banPlayer(player, "Banned: " + String.format(this.message,
                            etc.getDataSource().getItem(id)));
                } else {
                    handler.ban(etc.getDataSource().getItem(id));
                }
            
            } else if (!repeating || allowRepeat) {
                // Notify
                if (action.equalsIgnoreCase("notify")) {
                    handler.notifyAdmins(etc.getDataSource().getItem(id));
                
                // Log
                } else if (action.equalsIgnoreCase("log")) {
                    handler.log(etc.getDataSource().getItem(id));

                // Tell
                } else if (action.equalsIgnoreCase("tell")) {
                    if (this.message != null) {
                        player.sendMessage(Colors.Yellow +
                                String.format(message, etc.getDataSource().getItem(id))
                                + ".");
                    } else {
                        handler.tell(etc.getDataSource().getItem(id));
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Forget a player.
     *
     * @param player
     */
    public static void forgetPlayer(Player player) {
        lastAffected.remove(player.getName());
    }

    /**
     * Forget all players.
     *
     * @param player
     */
    public static void forgetAllPlayers() {
        lastAffected.clear();
    }

    /**
     * Gets called for actions.
     */
    private static interface ActionHandler {
        public void log(String itemName);
        public void kick(String itemName);
        public void ban(String itemName);
        public void notifyAdmins(String itemName);
        public void tell(String itemName);
    }
}
