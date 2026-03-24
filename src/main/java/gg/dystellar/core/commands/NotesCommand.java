package gg.dystellar.core.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.common.UserComponent;

/**
 * Lets you see the notes that the staff put on you, or lets you see someone else's notes if you're staff
 */
public class NotesCommand extends AbstractTargetPlayerCommand {

    public NotesCommand() {
		super("notes", "List a player's notes");
		this.requirePermission("dystellar.notes");
    }

	@Override
	protected void execute(CommandContext ctx, Ref<EntityStore> ref, Ref<EntityStore> ref2, PlayerRef p, World w, Store<EntityStore> store) {
		final var user = p.getHolder().getComponent(UserComponent.getComponentType());
		ctx.sender().sendMessage(Message.raw(p.getUsername() + "'s notes:"));
		for (final var note : user.notes)
			ctx.sender().sendMessage(Message.raw(" - " + note));
	}
}
