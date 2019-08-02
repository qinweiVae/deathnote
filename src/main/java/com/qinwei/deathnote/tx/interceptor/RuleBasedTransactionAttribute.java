package com.qinwei.deathnote.tx.interceptor;

import com.qinwei.deathnote.utils.ClassUtils;

import java.util.Arrays;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public class RuleBasedTransactionAttribute extends DefaultTransactionAttribute {

    private Class<?>[] rollbackFors;

    private Class<?>[] noRollbackFors;

    public RuleBasedTransactionAttribute() {
        super();
    }

    @Override
    public boolean rollbackOn(Throwable ex) {

        boolean rollback = false;
        if (this.rollbackFors != null) {
            rollback = Arrays.stream(this.rollbackFors).anyMatch(rule -> ClassUtils.isAssignable(rule, ex.getClass()));
        }

        if (rollback == false) {
            rollback = super.rollbackOn(ex);
        }

        if (this.noRollbackFors != null) {
            if (Arrays.stream(noRollbackFors).anyMatch(rule -> ClassUtils.isAssignable(rule, ex.getClass()))) {
                rollback = false;
            }
        }
        return rollback;
    }

    public void setRollbackFors(Class<?>[] rollbackFors) {
        this.rollbackFors = rollbackFors;
    }

    public void setNoRollbackFors(Class<?>[] noRollbackFors) {
        this.noRollbackFors = noRollbackFors;
    }
}
