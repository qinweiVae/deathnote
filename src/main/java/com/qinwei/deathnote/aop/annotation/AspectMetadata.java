package com.qinwei.deathnote.aop.annotation;

import com.qinwei.deathnote.aop.aspectj.Pointcut;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

/**
 * @author qinwei
 * @date 2019-07-01
 */
public class AspectMetadata {

    private final String aspectName;

    private AjType<?> ajType;

    private final Pointcut perClausePointcut;

    public AspectMetadata(Class<?> aspectClass, String aspectName) {
        this.aspectName = aspectName;
        Class<?> currentClass = aspectClass;
        while (currentClass != Object.class) {
            AjType<?> ajType = AjTypeSystem.getAjType(currentClass);
            if (ajType.isAspect()) {
                this.ajType = ajType;
                break;
            }
            currentClass = currentClass.getSuperclass();
        }
        if (this.ajType == null) {
            throw new IllegalStateException("Class '" + aspectClass.getName() + "' is not an @AspectJ aspect");
        }
        PerClauseKind perClauseKind = this.ajType.getPerClause().getKind();
        // perClauseKind 得到就是 @Aspect 注解中的值，默认是空 即 SINGLETON ，这里也只支持默认值
        switch (perClauseKind) {
            case SINGLETON:
                this.perClausePointcut = Pointcut.TRUE;
                return;
            default:
                throw new IllegalStateException("PerClause " + ajType.getPerClause().getKind() + " not supported by AOP for " + aspectClass);
        }
    }

    public AjType<?> getAjType() {
        return this.ajType;
    }

    public Class<?> getAspectClass() {
        return getAjType().getJavaClass();
    }

    public String getAspectName() {
        return this.aspectName;
    }

    public Pointcut getPerClausePointcut() {
        return this.perClausePointcut;
    }

}
