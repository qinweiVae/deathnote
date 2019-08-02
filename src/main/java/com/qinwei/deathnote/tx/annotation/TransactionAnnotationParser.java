package com.qinwei.deathnote.tx.annotation;

import com.qinwei.deathnote.context.annotation.AnnotationAttributes;
import com.qinwei.deathnote.tx.interceptor.RuleBasedTransactionAttribute;
import com.qinwei.deathnote.tx.interceptor.TransactionAttribute;
import com.qinwei.deathnote.utils.AnnotationUtils;

import java.lang.reflect.AnnotatedElement;

/**
 * @author qinwei
 * @date 2019-07-31
 */
public class TransactionAnnotationParser {

    public TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
        //拿到 @Transactional 的属性值
        AnnotationAttributes attributes = AnnotationUtils.getAnnotationAttributes(element, Transactional.class.getName(), false);
        // 如果存在 @Transactional ，解析
        if (attributes != null) {
            return parseTransactionAnnotation(attributes);
        }
        return null;
    }

    protected TransactionAttribute parseTransactionAnnotation(AnnotationAttributes attributes) {
        RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();

        Propagation propagation = attributes.getEnum("propagation");
        rbta.setPropagationBehavior(propagation.value());
        Isolation isolation = attributes.getEnum("isolation");
        rbta.setIsolationLevel(isolation.value());
        rbta.setTimeout(attributes.getNumber("timeout").intValue());
        rbta.setReadOnly(attributes.getBoolean("readOnly"));
        rbta.setQualifier(attributes.getString("transactionManager"));
        rbta.setRollbackFors(attributes.getClassArray("rollbackFor"));
        rbta.setNoRollbackFors(attributes.getClassArray("noRollbackFor"));

        return rbta;
    }
}
