package at.co.svc.agate.engine.call;

import java.util.HashMap;
import java.util.Map;

public class DialogManager {
    private static DialogManager instance = new DialogManager();
    // Key: instanc + ORD_ID + vpNummer + cardSlot
    private Map<String, String> cache = new HashMap<>();

    private DialogManager() {}
    public static DialogManager getInstance() { return instance; }

    public String getDialogId(String instanc, String ordId, String vpNummer, String cardSlot) {
        String key = instanc + "|" + ordId + "|" + vpNummer + "|" + cardSlot;
        return cache.get(key);
    }

    public void saveDialogId(String instanc, String ordId, String vpNummer, String cardSlot, String dialogId) {
        String key = instanc + "|" + ordId + "|" + vpNummer + "|" + cardSlot;
        cache.put(key, dialogId);
    }
}