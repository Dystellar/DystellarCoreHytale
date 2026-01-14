package gg.dystellar.core.perms;

public class Permission {

    private final String perm;
    private boolean value;

    public Permission(String perm, boolean value) {
        this.perm = perm;
        this.value = value;
    }

    public String getPerm() {
        return perm;
    }

    public boolean get() {
        return negate;
    }
}
