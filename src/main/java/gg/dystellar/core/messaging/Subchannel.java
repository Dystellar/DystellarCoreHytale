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

	SESSION(Handler::handleSession),
	DEMAND_FIND_PLAYER(Handler::handleDemFindPlayer),
	FRIEND_REMOVE(Handler::handleFriendRemove),
	REQUEST_ADDRESS(Handler::handleAddressRequest),
	DEFAULT_GROUP_UPDATE(Handler::handleDefaultGroupUpdate),
	USER_GROUP_UPDATE(Handler::handleUserGroupUpdate),
	GROUP_UPDATE(Handler::handleGroupUpdate),
	GROUP_CREATE(Handler::handleGroupCreate),
	GROUP_DELETE(Handler::handleGroupDelete)
	//TODO: INBOX_UPDATE((s, in) -> Handler.handleInboxUpdate(s, in)),
	//TODO: INBOX_SEND((s, in) -> Handler.handleInboxSend(s, in)),
	//TODO: INBOX_MANAGER_UPDATE((s, in) -> InboxCommand.get().init());
	;

	public final Optional<Receiver> callback;

	Subchannel(@Nullable Receiver callback) {
		this.callback = Optional.ofNullable(callback);
	}
}
