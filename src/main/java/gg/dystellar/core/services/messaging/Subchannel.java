package net.zylesh.dystellarcore.services.messaging;

import java.util.Optional;

import javax.annotation.Nullable;

import net.zylesh.dystellarcore.commands.InboxCommand;

public enum Subchannel {

	/**
	* WARNING! The plugin messaging protocol relies on enum ordinals for identiying types, DO NOT CHANGE the order of these or protocols will break.
	* If you need to register additional types, append them at the end.
	*/

	REGISTER((p, in) -> Handler.handleRegRes(p, in)),
    INBOX_UPDATE((p, in) -> Handler.handleInboxUpdate(p, in)),
    INBOX_MANAGER_UPDATE((p, in) -> InboxCommand.get().init()),
    GLOBAL_TAB_REGISTER(null),
    GLOBAL_TAB_UNREGISTER(null),
    DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK((p, in) -> Handler.handleDemIsPlayerWithinNetwork(p, in)),
    DEMAND_FIND_PLAYER((p, in) -> Handler.handleDemFindPlayerRes(p, in)),
    DEMAND_FIND_PLAYER_NOT_ONLINE((p, in) -> Handler.handleDemPlayerNotOnline(p, in)),
    INBOX_SEND((p, in) -> Handler.handleInboxSend(p, in)),
    REMOVE_PUNISHMENT_BY_ID((p, in) -> Handler.handleRemovePunishmentById(p, in)),
    PUNISHMENT_ADD_PROXY(null),
    PUNISHMENT_ADD_SERVER((p, in) -> Handler.handlePunishmentAddServer(p, in));

	public final Optional<PluginMessageCallback> callback;

	Subchannel(@Nullable PluginMessageCallback callback) {
		this.callback = Optional.ofNullable(callback);
	}
}
