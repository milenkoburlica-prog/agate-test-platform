package at.co.svc.agate.engine.gui;

import java.lang.ProcessHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Captures *only* processes descended from the exact Playwright driver.
 *
 * IMPORTANT: capture BEFORE killing the driver: once its PID has exited,
 * Windows may still show living Edge children but their ancestry can no
 * longer be enumerated reliably. Never find processes by name or kill the
 * user's ordinary Edge/Chrome processes.
 */
final class GuiOwnedProcessTree {
    private GuiOwnedProcessTree() {}

    record Owned(ProcessHandle handle, int depth) {}
    record Result(int signalled, List<Long> survivors) {
        boolean clean() { return survivors.isEmpty(); }
    }

    static List<Owned> capture(Process driver) {
        if (driver == null) return List.of();
        try {
            ProcessHandle root = driver.toHandle();
            try (Stream<ProcessHandle> descendants = root.descendants()) {
                return descendants.filter(h -> h.pid() != root.pid())
                        .map(h -> new Owned(h, Math.max(1, depth(h, root))))
                        .toList();
            }
        } catch (RuntimeException ex) {
            System.err.println("[GUI lifecycle] OWNED_TREE_DISCOVERY_ERROR cause=" + ex);
            return List.of();
        }
    }

    private static int depth(ProcessHandle child, ProcessHandle root) {
        int depth = 0;
        ProcessHandle cursor = child;
        for (int i = 0; i < 64; i++) {
            if (cursor.pid() == root.pid()) return depth;
            var parent = cursor.parent();
            if (parent.isEmpty()) return -1;
            depth++;
            cursor = parent.get();
        }
        return -1;
    }

    static List<Owned> merge(Collection<Owned> before, Collection<Owned> now) {
        Map<Long, Owned> all = new LinkedHashMap<>();
        for (Owned owned : before) all.put(owned.handle().pid(), owned);
        for (Owned owned : now) all.putIfAbsent(owned.handle().pid(), owned);
        return new ArrayList<>(all.values());
    }

    /**
     * Stop only descendants captured from the specific driver.
     * Deepest-first avoids losing child handles when their parent exits.
     * ProcessHandle retains process identity and prevents PID-reuse accidents.
     */
    static Result stopDescendants(List<Owned> snapshot, String ownerId, long timeoutMs) {
        return stopDescendants(snapshot, ownerId, timeoutMs, false);
    }

    static Result stopDescendants(List<Owned> snapshot, String ownerId, long timeoutMs,
                                  boolean verbose) {
        if (snapshot.isEmpty()) return new Result(0, List.of());
        int signalled = 0;
        List<Owned> descendants = snapshot.stream()
                .sorted(Comparator.comparingInt(Owned::depth).reversed())
                .toList();
        for (Owned owned : descendants) {
            ProcessHandle process = owned.handle();
            if (!process.isAlive()) continue;
            try {
                if (process.destroyForcibly()) signalled++;
            } catch (RuntimeException ex) {
                System.err.println("[GUI lifecycle] OWNED_CHILD_STOP_ERROR owner=" + ownerId
                        + " pid=" + process.pid() + " cause=" + ex);
            }
        }
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        List<Long> stillAlive = alivePids(descendants);
        while (!stillAlive.isEmpty() && System.nanoTime() < deadline) {
            try { Thread.sleep(20); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); break; }
            stillAlive = alivePids(descendants);
        }
        GuiLifecycleLog.detail(verbose, "[GUI lifecycle] OWNED_CHILD_TREE_CLEANUP owner=" + ownerId
                + " captured=" + snapshot.size() + " signalled=" + signalled
                + " survivors=" + stillAlive);
        return new Result(signalled, stillAlive);
    }

    static List<Long> alivePids(List<Owned> observed) {
        return observed.stream().map(Owned::handle).filter(ProcessHandle::isAlive)
                .map(ProcessHandle::pid).distinct().toList();
    }
}
