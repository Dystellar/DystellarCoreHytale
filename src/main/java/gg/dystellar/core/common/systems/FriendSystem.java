package gg.dystellar.core.common.systems;

import gg.dystellar.core.api.comms.Channel;

public final class FriendSystem {
	private FriendSystem() {}

	public 

	switch (strings[0]) {
            case "add": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /f add <player>");
                    return true;
                }
                if (cooldowns.contains(p.getUniqueId())) {
                    p.sendMessage(Msgs.ON_COOLDOWN.replace("<seconds>", "20"));
                    return true;
                }
                requestsCache.add(p.getUniqueId());
                DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.FRIEND_ADD_REQUEST, strings[1]);
                break;
            }
            case "remove": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /f remove <player>");
                    return true;
                }
                Player pInt = Bukkit.getPlayer(strings[1]);
                User u = User.get(p);
                if (pInt != null && pInt.isOnline()) {
                    if (!u.friends.remove(pInt.getUniqueId())) {
                        p.sendMessage(Msgs.PLAYER_NOT_ON_FRIENDS_LIST);
                        return true;
                    }
                    User uInt = User.get(pInt);
                    uInt.friends.remove(u.getUUID());
                    p.sendMessage(Msgs.FRIEND_REMOVED_SENDER.replace("<player>", pInt.getName()));
                    pInt.sendMessage(Msgs.FRIEND_REMOVED_RECEIVER.replace("<player>", p.getName()));
                } else {
                    DystellarCore.getAsyncManager().submit(() -> {
                        UUID uuid;
                        if (uuidsCache.containsKey(p)) {
                            uuid = uuidsCache.get(p).get(strings[1]);
                        } else {
                            uuid = MariaDB.loadUUID(strings[1]);
                            Map<String, UUID> map = new HashMap<>();
                            map.put(strings[1], uuid);
                            uuidsCache.put(p, map);
                        }
                        if (uuid == null) {
                            p.sendMessage(Msgs.ERROR_PLAYER_NOT_FOUND);
                            return;
                        }
                        if (!u.friends.remove(uuid)) {
                            p.sendMessage(Msgs.PLAYER_NOT_ON_FRIENDS_LIST);
                            return;
                        }
                        DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.REMOVE_FRIEND, uuid.toString());
                    });
                }
                break;
            }
            case "find": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /f find <player>");
                    return true;
                }
                Player pInt = Bukkit.getPlayer(strings[1]);
                if (pInt != null && pInt.isOnline()) {
                    p.sendMessage(Msgs.FIND_SAME_SERVER_AS_SENDER.replace("<player>", pInt.getName()));
                    return true;
                } else {
                    DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.DEMAND_FIND_PLAYER, strings[1]);
                }
                break;
            }
            case "list": {
                if (cooldowns.contains(p.getUniqueId())) {
                    p.sendMessage(Msgs.ON_COOLDOWN.replace("<seconds>", "20"));
                    return true;
                }
                cooldowns.add(p.getUniqueId());
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> cooldowns.remove(p.getUniqueId()), 400L);
                User u = User.get(p);
                DystellarCore.getAsyncManager().submit(() -> {
                    List<String> friends = new ArrayList<>();
                    synchronized (u.friends) {
                        for (UUID uuid : u.friends) {
                            User us = User.get(uuid);
                            if (us == null) {
                                String name = MariaDB.loadName(uuid.toString());
                                if (name != null) friends.add(name);
                            } else {
                                friends.add(us.getName());
                            }
                        }
                    }
                    p.sendMessage(ChatColor.DARK_GREEN + "Friends list:");
                    for (String st : friends) {
                        p.sendMessage(" - " + ChatColor.DARK_AQUA + st);
                    }
                });
                break;
            }
            case "accept": {
                if (!DystellarCore.getInstance().requests.containsKey(p.getUniqueId())) {
                    p.sendMessage(Msgs.FRIEND_REQUEST_EXPIRED);
                    return true;
                }
                User u = User.get(p);
                boolean sendTip = false;
                if (u.friends.isEmpty() && u.tipsSent[Consts.FIRST_FRIEND_TIP_POS] == Consts.BYTE_FALSE) {
                    u.tipsSent[Consts.FIRST_FRIEND_TIP_POS] = Consts.BYTE_TRUE;
                    sendTip = true;
                }
                UUID uuid = DystellarCore.getInstance().requests.remove(p.getUniqueId());
                u.friends.add(uuid);
                p.sendMessage(Msgs.FRIEND_REQUEST_ACCEPTED_RECEIVER);
                if (sendTip) p.sendMessage(Consts.FIRST_FRIEND_TIP_MSG);
                DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.FRIEND_ADD_REQUEST_ACCEPT, uuid.toString());
                break;
            }
            case "reject": {
                if (!DystellarCore.getInstance().requests.containsKey(p.getUniqueId())) {
                    p.sendMessage(Msgs.FRIEND_REQUEST_EXPIRED);
                    return true;
                }
                UUID uuid = DystellarCore.getInstance().requests.remove(p.getUniqueId());
                p.sendMessage(Msgs.FRIEND_REQUEST_REJECTED_RECEIVER);
                DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.FRIEND_ADD_REQUEST_REJECT, uuid.toString());
                break;
            }
            case "togglerequests": {
                User u = User.get(p);
                byte setting = u.extraOptions[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS];
                if (setting == Consts.BYTE_FALSE) {
                    u.extraOptions[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS] = Consts.BYTE_TRUE;
                    p.sendMessage(Msgs.FRIEND_REQUESTS_ENABLED);
                } else {
                    u.extraOptions[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS] = Consts.BYTE_FALSE;
                    p.sendMessage(Msgs.FRIEND_REQUESTS_DISABLED);
                }
                break;
            }
        }
}
