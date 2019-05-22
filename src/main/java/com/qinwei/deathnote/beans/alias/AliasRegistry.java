package com.qinwei.deathnote.beans.alias;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public interface AliasRegistry {

    void registerAlias(String name, String alias);

    void removeAlias(String alias);

    boolean isAlias(String name);

    String[] getAliases(String name);
}
