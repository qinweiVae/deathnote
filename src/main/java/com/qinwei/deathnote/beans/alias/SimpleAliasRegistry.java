package com.qinwei.deathnote.beans.alias;

/**
 * @author qinwei
 * @date 2019-05-22
 */
public class SimpleAliasRegistry implements AliasRegistry {



    @Override
    public void registerAlias(String name, String alias) {

    }

    @Override
    public void removeAlias(String alias) {

    }

    @Override
    public boolean isAlias(String name) {
        return false;
    }

    @Override
    public String[] getAliases(String name) {
        return new String[0];
    }
}
