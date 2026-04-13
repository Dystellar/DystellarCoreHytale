package gg.dystellar.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gg.dystellar.core.common.User;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.perms.Permission;

public final class PermissionTests {

	private User user;

    private void setPerms(String... pairs) {
        user.perms.clear();
        for (int i = 0; i < pairs.length; i += 2) {
            String node = pairs[i];
            boolean val = Boolean.parseBoolean(pairs[i + 1]);
            user.perms.put(node, new Permission(node, val));
        }
    }
 
    @BeforeEach
    void setUp() {
        // Construct a bare User with no group
        user = new User(null, null, null);
        user.group = Optional.empty();
    }
 
    // ── Exact match ──────────────────────────────────────────────────────────
 
    @Test
    void exactMatch_granted() {
        setPerms("a.b", "true");
        assertTrue(user.hasPermission("a.b"));
    }
 
    @Test
    void exactMatch_denied() {
        setPerms("a.b", "false");
        assertFalse(user.hasPermission("a.b"));
    }
 
    @Test
    void exactMatch_deepNode_granted() {
        setPerms("a.b.c.d", "true");
        assertTrue(user.hasPermission("a.b.c.d"));
    }
 
    @Test
    void noMatch_returnsFalse() {
        setPerms("a.b", "true");
        assertFalse(user.hasPermission("x.y"));
    }
 
    // ── Global wildcard * ────────────────────────────────────────────────────
 
    @Test
    void globalWildcard_matchesArbitraryPerm() {
        setPerms("*", "true");
        assertTrue(user.hasPermission("a.b"));
        assertTrue(user.hasPermission("x.y.z"));
        assertTrue(user.hasPermission("foo.bar.baz.qux"));
    }
 
    @Test
    void globalWildcard_denied_blocksAll() {
        setPerms("*", "false");
        assertFalse(user.hasPermission("a.b"));
        assertFalse(user.hasPermission("x.y.z"));
    }
 
    @Test
    void exactMatch_beatsGlobalWildcard() {
        setPerms("*", "false", "a.b", "true");
        assertTrue(user.hasPermission("a.b"));   // exact wins
        assertFalse(user.hasPermission("a.c"));  // falls back to *
    }
 
    // ── Prefix wildcard a.b.* ────────────────────────────────────────────────
 
    @Test
    void prefixWildcard_twoLevels_matchesChildren() {
        setPerms("a.b.*", "true");
        assertTrue(user.hasPermission("a.b.c"));
        assertTrue(user.hasPermission("a.b.d"));
    }
 
    @Test
    void prefixWildcard_twoLevels_doesNotMatchParent() {
        // a.b.* should NOT match a.b itself (2-part node, loop never emits a.b.*)
        setPerms("a.b.*", "true");
        assertFalse(user.hasPermission("a.b"));
    }
 
    @Test
    void prefixWildcard_twoLevels_matchesDeeperNodes() {
        // a.b.* is recursive downward: a.b.c.d should also match
        setPerms("a.b.*", "true");
        assertTrue(user.hasPermission("a.b.c.d"));
        assertTrue(user.hasPermission("a.b.c.d.e"));
    }
 
    @Test
    void prefixWildcard_twoLevels_doesNotMatchSibling() {
        setPerms("a.b.*", "true");
        assertFalse(user.hasPermission("a.c.d"));
    }
 
    // ── Prefix wildcard a.* ──────────────────────────────────────────────────
 
    @Test
    void prefixWildcard_oneLevel_matchesDirectChildren() {
        setPerms("a.*", "true");
        assertTrue(user.hasPermission("a.b"));
        assertTrue(user.hasPermission("a.c"));
    }
 
    @Test
    void prefixWildcard_oneLevel_matchesDeeperNodes() {
        // a.* is also recursive: a.b.c should match (loop reaches a.* at i=0)
        setPerms("a.*", "true");
        assertTrue(user.hasPermission("a.b.c"));
        assertTrue(user.hasPermission("a.b.c.d"));
    }
 
    @Test
    void prefixWildcard_oneLevel_doesNotMatchUnrelated() {
        setPerms("a.*", "true");
        assertFalse(user.hasPermission("b.c"));
    }
 
    // ── Priority ordering ────────────────────────────────────────────────────
 
    @Test
    void priority_exactBeatsAllWildcards() {
        setPerms("*", "false", "a.*", "false", "a.b.*", "false", "a.b.c", "true");
        assertTrue(user.hasPermission("a.b.c"));
    }
 
    @Test
    void priority_specificPrefixBeatsLessSpecific() {
        setPerms("a.*", "false", "a.b.*", "true");
        assertTrue(user.hasPermission("a.b.c"));  // a.b.* beats a.*
    }
 
    @Test
    void priority_oneLevelPrefixBeatsGlobal() {
        setPerms("*", "false", "a.*", "true");
        assertFalse(user.hasPermission("a.b"));
    }
 
    // ── Negation via exact override ──────────────────────────────────────────
 
    @Test
    void negation_exactDenyOverridesPrefixGrant() {
        setPerms("a.*", "true", "a.b", "false");
        assertFalse(user.hasPermission("a.b"));  // exact deny wins
        assertTrue(user.hasPermission("a.c"));   // still covered by a.*
    }
 
    @Test
    void negation_specificPrefixDenyOverridesWiderGrant() {
        setPerms("a.*", "true", "a.b.*", "false");
        assertFalse(user.hasPermission("a.b.c")); // a.b.* deny is more specific
        assertTrue(user.hasPermission("a.c"));    // a.* grant still applies
    }
 
    // ── Group permissions (fallback) ─────────────────────────────────────────
 
    @Test
    void groupPerm_usedWhenNoUserPerm() {
        Group group = new Group("test", "test", "test", List.of(new Permission("a.b", true)));
        user.group = Optional.of(group);
 
        assertTrue(user.hasPermission("a.b"));
    }
 
    @Test
    void userPerm_overridesGroupPerm() {
        Group group = new Group("test", "test", "test", List.of(new Permission("a.b", true)));
        user.group = Optional.of(group);
        user.perms.put("a.b", new Permission("a.b", false));
 
        assertFalse(user.hasPermission("a.b"));
    }
 
    @Test
    void groupWildcard_appliesToUser() {
        Map<String, Permission> groupPerms = new HashMap<>();
        groupPerms.put("a.*", new Permission("a.*", true));
 
        Group group = new Group("test", "test", "test", List.of(new Permission("a.*", true)));
        user.group = Optional.of(group);
 
        assertTrue(user.hasPermission("a.b"));
        assertTrue(user.hasPermission("a.b.c"));
    }
}
