package com.zyndev.tool.fastsql.core;

import com.zyndev.tool.fastsql.annotation.EnableFastSql;
import com.zyndev.tool.fastsql.util.ClassScanner;
import com.zyndev.tool.fastsql.util.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The type Fast sql repository registrar.
 *
 * @author 张瑀楠 zyndev@gmail.com
 * @version 1.0
 * @date 2018 /2/23 12:26
 */
public class FastSqlRepositoryRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

    private ConfigurableListableBeanFactory beanFactory;

    @Value("${fastsql.showSql}")
    private boolean showSql;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        GlobalConfig.setShowSql(showSql);

        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableFastSql.class.getName()));

        Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
        if (!Annotation.class.equals(annotationClass)) {
            System.out.println("ddd");
        }

        List<String> basePackages = new ArrayList<>();
        for (String pkg : annoAttrs.getStringArray("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : annoAttrs.getStringArray("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        ClassScanner classScanner = new ClassScanner();
        Set<Class<?>> classSet = null;

        try {
            classSet = classScanner.getPackageAllClasses(basePackages.get(0), true);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (Class clazz : classSet) {
            if (clazz.getAnnotation(Repository.class) != null) {
                beanFactory.registerSingleton(StringUtil.firstCharToLowerCase(clazz.getSimpleName()), FacadeProxy.newMapperProxy(clazz));
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }
}
