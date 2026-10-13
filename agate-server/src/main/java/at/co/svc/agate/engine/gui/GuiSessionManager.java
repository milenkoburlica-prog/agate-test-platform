package at.co.svc.agate.engine.gui;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;

/** An ExecutionContext owns its browser sessions, never the global DEFAULT label. */
public final class GuiSessionManager {
    private record Key(ExecutionContext execution, String name) {}
    private record OwnedSession(long caller, GuiSession session) {}
    private static final Map<Key, OwnedSession> sessions = new ConcurrentHashMap<>();
    private static final Map<ExecutionContext, GuiRunScope> scopes = new ConcurrentHashMap<>();
    public static void bind(ExecutionContext context, GuiRunScope scope) {
        if (scope != null) scopes.put(context, scope);
    }
    public static void unbind(ExecutionContext context) { scopes.remove(context); }
    static void closeScope(GuiRunScope scope) {
        for (var entry : new ArrayList<>(scopes.entrySet())) {
            if (entry.getValue() == scope) {
                close(entry.getKey(), null);
                scopes.remove(entry.getKey(), scope);
            }
        }
    }
    private GuiSessionManager() {}
    public static GuiSession get(ExecutionContext execution, String name) {
        Key key = new Key(execution, name);
        return sessions.computeIfAbsent(key, ignored -> new OwnedSession(Thread.currentThread().getId(), scopes.containsKey(execution)
                ? scopes.get(execution).newSession() : new GuiSession())).session();
    }
    public static void close(ExecutionContext execution, String name) {
        remove((key, owned) -> key.execution() == execution && (name == null || key.name().equals(name)));
    }
    // Backwards compatible runner cleanup: only sessions created by this caller.
    public static void closeLegacy(String name) {
        long caller = Thread.currentThread().getId();
        remove((key, owned) -> key.name().equals(name) && owned.caller() == caller);
    }
    public static void closeAll() { remove((key, owned) -> true); scopes.clear(); }
    private static void remove(java.util.function.BiPredicate<Key, OwnedSession> matches) {
        for (var entry : new ArrayList<>(sessions.entrySet())) {
            Key key = entry.getKey(); OwnedSession owned = entry.getValue();
            if (matches.test(key, owned) && sessions.remove(key, owned)) owned.session().close();
        }
    }
}
