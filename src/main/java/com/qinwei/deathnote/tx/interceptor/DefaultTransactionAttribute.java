package com.qinwei.deathnote.tx.interceptor;

import com.qinwei.deathnote.tx.transaction.DefaultTransactionDefinition;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {

    private String qualifier;

    private String descriptor;

    public DefaultTransactionAttribute() {
        super();
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public String getQualifier() {
        return this.qualifier;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    @Override
    public boolean rollbackOn(Throwable ex) {
        return (ex instanceof RuntimeException || ex instanceof Error);
    }
}
