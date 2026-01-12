package net.zylesh.dystellarcore.perms;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachmentInfo;

import net.zylesh.dystellarcore.core.User;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomPermBase extends PermissibleBase {

    private static final String OP_PERM = "minecraft.command.op";
    private final Permissible permissible;

    private final User user;
    public CustomPermBase(Permissible permissible, User user) {
        super(permissible);
        this.user = user;
        this.permissible = permissible;
    }

    private static Permission getMatch(String inName, Group group) {
        if (group == null)
            return null;

        inName = inName.toLowerCase();

        Permission perm = group.getPermissions().get(inName);
        if (perm != null)
            return perm;

        String[] split = inName.split("\\.");

        for (Permission info : group.getPermissions().values()) {
            if (!info.getPerm().contains("*"))
                continue;
            String[] permSplit = info.getPerm().split("\\.");

            int min = Math.min(split.length, permSplit.length);

            for (int j = 0; j < min; j++) {
                if (permSplit[j].equals("*"))
                    return info;
                if (!permSplit[j].equalsIgnoreCase(split[j]))
                    break;
            }
        }

        return null;
    }

    @Override
    public boolean hasPermission(String inName) {
        if (inName == null)
            return false;

        if (inName.equalsIgnoreCase(OP_PERM))
            return false;
        Permission info = getMatch(inName, user.getGroup());
        if (info != null)
            return !info.isNegate();

        return super.hasPermission(inName);
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {}

    @Override
    public boolean isPermissionSet(String name) {
        return getMatch(name, user.getGroup()) != null || super.isPermissionSet(name);
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        if (user.getGroup() == null)
            return new HashSet<>();
        return user.getGroup().getPermissions().values().stream().map(permission -> new PermissionAttachmentInfo(permissible, permission.getPerm(), null, !permission.isNegate())).collect(Collectors.toSet());
    }
}
