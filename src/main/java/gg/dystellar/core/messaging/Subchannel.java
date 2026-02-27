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

	SESSION((s, in) -> Handler.handleSession(s, in)),
	DEMAND_FIND_PLAYER((s, in) -> Handler.handleDemFindPlayer(s, in)),
	REMOVE_PUNISHMENT_BY_ID((s, in) -> Handler.handleRemovePunishmentById(s, in)),
	PUNISHMENT_ADD_PROXY(null),
	PUNISHMENT_ADD_SERVER((s, in) -> Handler.handlePunishmentAddServer(s, in))
	//TODO: INBOX_UPDATE((s, in) -> Handler.handleInboxUpdate(s, in)),
	//TODO: INBOX_SEND((s, in) -> Handler.handleInboxSend(s, in)),
	//TODO: INBOX_MANAGER_UPDATE((s, in) -> InboxCommand.get().init());
	;

	public final Optional<Receiver> callback;

	Subchannel(@Nullable Receiver callback) {
		this.callback = Optional.ofNullable(callback);
	}
}
