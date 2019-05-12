package com.qinwei.deathnote.support.scan;

import java.util.Set;

/**
 * @author qinwei
 * @date 2019-05-12 14:42
 */
public interface Scanner {

    Set<Class> scan(String basePackage);
}
