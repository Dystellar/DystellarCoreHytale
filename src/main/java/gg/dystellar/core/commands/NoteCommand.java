package gg.dystellar.core.commands;

import java.awt.Color;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.common.User;

/**
 * This command on itself doesn't do anything, it just lets you annotate something useful about a player,
 * like an explanation or an observation, only the staff and the affected player will see them.
 */
public class NoteCommand extends AbstractTargetPlayerCommand {
	private final RequiredArg<String> noteArg = this.withRequiredArg("note", "Note to add", ArgTypes.STRING);

    public NoteCommand() {
		super("note", "Add a note to a user");
		this.requirePermission("dystellar.notes");
    }

	@Override
	protected void execute(CommandContext ctx, Ref<EntityStore> ref, Ref<EntityStore> ref2, PlayerRef p, World w, Store<EntityStore> store) {
		final var note = ctx.get(noteArg);
		final var user = User.getUser(p).get();
		user.notes.add(note);
		ctx.sender().sendMessage(Message.raw("Note added!").color(new Color(0x00FF00)));
	}
}
