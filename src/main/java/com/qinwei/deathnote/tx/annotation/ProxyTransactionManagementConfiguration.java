package com.qinwei.deathnote.tx.annotation;

import com.qinwei.deathnote.context.annotation.Bean;
import com.qinwei.deathnote.context.annotation.Configuration;
import com.qinwei.deathnote.tx.interceptor.AnnotationTransactionAttributeSource;
import com.qinwei.deathnote.tx.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import com.qinwei.deathnote.tx.interceptor.TransactionAttributeSource;
import com.qinwei.deathnote.tx.interceptor.TransactionInterceptor;

/**
 * @author qinwei
 * @date 2019-07-30
 */
@Configuration
public class ProxyTransactionManagementConfiguration {

    @Bean
    public TransactionAttributeSource transactionAttributeSource() {
        return new AnnotationTransactionAttributeSource();
    }

    @Bean
    public TransactionInterceptor transactionInterceptor() {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(transactionAttributeSource());
        return interceptor;
    }

    @Bean("internalTransactionAdvisor")
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor() {
        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource());
        advisor.setAdvice(transactionInterceptor());
        return advisor;
    }

}
