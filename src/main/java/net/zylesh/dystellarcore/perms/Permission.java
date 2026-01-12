package net.zylesh.dystellarcore.perms;

public class Permission {

    private final String perm;
    private boolean negate;

    public Permission(String perm, boolean negate) {
        this.perm = perm;
        this.negate = negate;
    }

    public String getPerm() {
        return perm;
    }

    public boolean isNegate() {
        return negate;
    }
}
