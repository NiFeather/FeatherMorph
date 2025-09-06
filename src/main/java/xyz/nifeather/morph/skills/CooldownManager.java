package xyz.nifeather.morph.skills;

import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphPluginObject;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager extends MorphPluginObject
{
    private final Map<UUID, CooldownStore> storeMap = new ConcurrentHashMap<>();

    private CooldownStore getStore(UUID uuid)
    {
        var store = storeMap.getOrDefault(uuid, null);
        if (store == null)
        {
            store = new CooldownStore();
            storeMap.put(uuid, store);
        }

        return store;
    }

    public synchronized void submit(UUID uuid, String disguiseIdentifier, long availableAfter)
    {
        getStore(uuid).put(disguiseIdentifier, availableAfter);
    }

    public synchronized long pull(UUID uuid, String disguiseIdentifier)
    {
        return getStore(uuid).get(disguiseIdentifier);
    }

    public synchronized void trim()
    {
        for (Map.Entry<UUID, CooldownStore> pair : new HashSet<>(storeMap.entrySet()))
        {
            if (pair.getValue().trim())
                storeMap.remove(pair.getKey());
        }
    }

    public static class CooldownStore
    {
        // Disguise Identifier <-> Available After ... Tick
        private final Map<String, Long> cooldownMap = new ConcurrentHashMap<>();

        public void put(String id, long availableAfter)
        {
            cooldownMap.put(id, availableAfter);
        }

        /**
         * @return 0 if not set
         */
        public long get(String id)
        {
            if (cooldownMap.containsKey(id))
                return cooldownMap.remove(id);

            return 0L;
        }

        /**
         * @return TRUE if this cooldownStore no longer holds any offline cooldowns
         */
        public boolean trim()
        {
            var currentTick = FeatherMorphMain.getInstance().getCurrentTick();

            HashSet<Map.Entry<String, Long>> entries = new HashSet<>(cooldownMap.entrySet());
            var list = entries.stream()
                    .filter(entry -> currentTick > entry.getValue())
                    .toList();

            list.forEach(entrySet -> cooldownMap.remove(entrySet.getKey()));
            return cooldownMap.isEmpty();
        }
    }
}
