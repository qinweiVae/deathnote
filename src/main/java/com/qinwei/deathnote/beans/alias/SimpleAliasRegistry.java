package com.qinwei.deathnote.beans.alias;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public class SimpleAliasRegistry implements AliasRegistry {

    private final Map<String, String> aliasMap = new ConcurrentHashMap<>(32);

    @Override
    public void registerAlias(String name, String alias) {
        assert !StringUtils.isEmpty(name) : "name can not be null";
        assert !StringUtils.isEmpty(alias) : "alias can not be null";
        String registeredName = aliasMap.get(alias);
        if (registeredName != null) {
            if (registeredName.equals(name)) {
                return;
            }
            throw new IllegalStateException("Cannot register alias '" + alias + "' for name '" +
                    name + "': It is already registered for name '" + registeredName + "'.");
        }
        checkForAliasCircle(name, alias);
        aliasMap.put(alias, name);
    }

    private void checkForAliasCircle(String name, String alias) {
        if (hasAlias(alias, name)) {
            throw new IllegalStateException("Cannot register alias '" + alias +
                    "' for name '" + name + "': Circular reference - '" +
                    name + "' is a direct or indirect alias for '" + alias + "' already");
        }
    }

    private boolean hasAlias(String alias, String name) {
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            String registeredName = entry.getValue();
            if (registeredName.equals(alias)) {
                String registeredAlias = entry.getKey();
                return (registeredAlias.equals(name)) || hasAlias(registeredAlias, name);
            }
        }
        return false;
    }

    @Override
    public void removeAlias(String alias) {
        aliasMap.remove(alias);
    }

    @Override
    public boolean isAlias(String name) {
        return aliasMap.containsKey(name);
    }

    @Override
    public String[] getAliases(String name) {
        List<String> result = new ArrayList<>();
        synchronized (aliasMap) {
            retrieveAliases(name, result);
        }
        return result.toArray(new String[result.size()]);
    }

    private void retrieveAliases(String name, List<String> result) {
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            String registeredName = entry.getValue();
            if (registeredName.equals(name)) {
                String alias = entry.getKey();
                result.add(alias);
                //retrieveAliases(alias, result);
            }
        }
    }

    public String canonicalName(String name) {
        String canonicalName = name;
        String resolvedName;
        do {
            resolvedName = aliasMap.get(canonicalName);
            if (resolvedName != null) {
                canonicalName = resolvedName;
            }
        } while (resolvedName != null);
        return canonicalName;
    }

}
