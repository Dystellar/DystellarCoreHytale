package gg.dystellar.core.messaging;

import java.util.Optional;

import javax.annotation.Nullable;

import gg.dystellar.core.api.comms.Receiver;

/**
 * Subchannel protocol, already implemented callbacks for each type of message
 */
public enum Subchannel {

	/**
	* WARNING! The plugin messaging protocol relies on enum ordinals for identiying types, DO NOT CHANGE the order of these or protocols will break.
	* If you need to register additional types, append them at the end.
	*/

	REGISTER((s, in) -> Handler.handleRegRes(s, in)),
    INBOX_UPDATE((s, in) -> Handler.handleInboxUpdate(s, in)),
    INBOX_MANAGER_UPDATE((s, in) -> InboxCommand.get().init()),
    GLOBAL_TAB_REGISTER(null),
    GLOBAL_TAB_UNREGISTER(null),
    DEMAND_IS_PLAYER_ONLINE_WITHIN_NETWORK((s, in) -> Handler.handleDemIsPlayerWithinNetwork(s, in)),
    DEMAND_FIND_PLAYER((s, in) -> Handler.handleDemFindPlayerRes(s, in)),
    DEMAND_FIND_PLAYER_NOT_ONLINE((s, in) -> Handler.handleDemPlayerNotOnline(s, in)),
    INBOX_SEND((s, in) -> Handler.handleInboxSend(s, in)),
    REMOVE_PUNISHMENT_BY_ID((s, in) -> Handler.handleRemovePunishmentById(s, in)),
    PUNISHMENT_ADD_PROXY(null),
    PUNISHMENT_ADD_SERVER((s, in) -> Handler.handlePunishmentAddServer(s, in));

	public final Optional<Receiver> callback;

	Subchannel(@Nullable Receiver callback) {
		this.callback = Optional.ofNullable(callback);
	}
}
