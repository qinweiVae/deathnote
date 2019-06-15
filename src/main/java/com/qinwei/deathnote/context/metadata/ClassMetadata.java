package com.qinwei.deathnote.context.metadata;

/**
 * @author qinwei
 * @date 2019-06-14
 */
public interface ClassMetadata {

    String getClassName();

    boolean isInterface();

    boolean isAnnotation();

    boolean isAbstract();

    boolean isConcrete();

    boolean isFinal();

    /**
     * 该类是在那个类中定义的， 比如直接定义的内部类或匿名内部类
     */
    boolean hasEnclosingClass();

    String getEnclosingClassName();

    boolean hasSuperClass();

    String getSuperClassName();

    String[] getInterfaceNames();

    /**
     * 获取当前类定义所在的类
     */
    String[] getMemberClassNames();

}
