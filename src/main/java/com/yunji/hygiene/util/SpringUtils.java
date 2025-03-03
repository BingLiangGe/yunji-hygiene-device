package com.yunji.hygiene.util;

import org.springframework.aop.framework.AopContext;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * spring工具类 方便在非spring管理环境中获取bean
 * 
 * @author yunji
 */
@Component
public final class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware 
{
    /** Spring应用上下文环境 */
    private static ConfigurableListableBeanFactory beanFactory;

    private static ApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException 
    {
        SpringUtils.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException 
    {
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 获取对象
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException
    {
        return (T) beanFactory.getBean(name);
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param clz
     * @return
     * @throws BeansException
     *
     */
    public static <T> T getBean(Class<T> clz) throws BeansException
    {
        return (T) beanFactory.getBean(clz);
    }

    /**
     * @Title	contains
     * @Intro	检查是否存在bean对象
     * @Date    2024-05-02 18:18:38.968
     * @param	name bean名称
     * @return	boolean 与所给名称匹配的bean定义则返回true
     */
    public static boolean contains(String name)
    {
        return beanFactory.containsBean(name);
    }

    /**
     * @Title	register
     * @Intro	注册bean对象
     * @Date    2024-05-02 19:53:20.025
     * @param	clazz 类对象
     */
    public static void register(Class<?> clazz)
    {
        register(clazz.getName(), clazz);
    }

    /**
     * @Title	register
     * @Desc	注册bean对象
     * @Date	2024-05-06 10:58:05.664
     * @param	alias bean 别名
     * @param	clazz 类对象
     * @return	T 对象实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T register(String alias, Class<T> clazz, Object... args)
    {
        // BeanFactory最重要的一个实现类 --> DefaultListableBeanFactory
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        // bean 的定义(class, scope, 初始化, 销毁)
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(clazz);

        if (BeanUtils.isNotNull(args))
        {
            for (Object arg: args)
            {
                beanDefinition.addConstructorArgValue(arg);
            }
        }
        // 将bean定义放入刚刚定义好的beanFactory工厂里面去注册register
        beanFactory.registerBeanDefinition(alias, beanDefinition.getBeanDefinition());
        return (T) beanFactory.getBean(alias);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws NoSuchBeanDefinitionException
     *
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException
    {
        return beanFactory.isSingleton(name);
    }

    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws NoSuchBeanDefinitionException
     *
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException
    {
        return beanFactory.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     *
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException
    {
        return beanFactory.getAliases(name);
    }

    /**
     * 获取aop代理对象
     * 
     * @param invoker
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker)
    {
        Object o = AopContext.currentProxy();
        Class<?> aClass = AopProxyUtils.ultimateTargetClass(o);
        if(aClass == invoker.getClass()){
            return (T) o;
        }
        return invoker;
    }

    /**
     * 获取当前的环境配置，无配置返回null
     *
     * @return 当前的环境配置
     */
    public static String[] getActiveProfiles()
    {
        return applicationContext.getEnvironment().getActiveProfiles();
    }

    /**
     * 获取当前的环境配置，当有多个环境配置时，只获取第一个
     *
     * @return 当前的环境配置
     */
    public static String getActiveProfile()
    {
        final String[] activeProfiles = getActiveProfiles();
        return BeanUtils.isNull(activeProfiles) ? activeProfiles[0] : null;
    }

    /**
     * 获取配置文件中的值
     *
     * @param key 配置文件的key
     * @return 当前的配置文件的值
     *
     */
    public static String getRequiredProperty(String key)
    {
        return applicationContext.getEnvironment().getRequiredProperty(key);
    }
}
