package gg.dystellar.core.perms;

import java.util.function.Supplier;

public class Permission implements Supplier<Boolean> {

    private final String perm;
    private boolean value;

    public Permission(String perm, boolean value) {
        this.perm = perm;
        this.value = value;
    }

    public String getPerm() {
        return perm;
    }

    public Boolean get() {
        return value;
    }
}
