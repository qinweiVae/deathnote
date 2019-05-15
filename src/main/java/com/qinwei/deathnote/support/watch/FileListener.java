package com.qinwei.deathnote.support.watch;

import java.nio.file.Path;

/**
 * @author qinwei
 * @date 2019-05-15
 */
public interface FileListener {

    void changed(Path path);
}
