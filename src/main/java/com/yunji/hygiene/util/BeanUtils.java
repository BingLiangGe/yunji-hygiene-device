package com.yunji.hygiene.util;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Bean 工具类
 *
 * @author yunji
 */
public final class BeanUtils {
    private BeanUtils() {
    }

    private static final Logger logs = LoggerFactory.getLogger(BeanUtils.class);

    /**
     * Bean方法名中属性名开始的下标
     */
    private static final int BEAN_METHOD_PROP_INDEX = 3;

    /**
     * 匹配getter方法的正则表达式
     */
    private static final Pattern GET_PATTERN = Pattern.compile("get(\\p{javaUpperCase}\\w*)");

    /**
     * 匹配setter方法的正则表达式
     */
    private static final Pattern SET_PATTERN = Pattern.compile("set(\\p{javaUpperCase}\\w*)");

    private static final ConcurrentHashMap<String, Constructor<?>> CACHE = new ConcurrentHashMap<>();


    public static void copyExcludeBase(Object source, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(source, target, "creator", "createName", "createTime");
    }

    /**
     * @Title clone
     * @Desc Bean属性复制工具方法
     * @Date 2024-03-29 10:50:11
     * @Param source 源对象
     * @Param target 目标对象
     */
    public static void clone(Object source, Object target) {
        try {
            org.springframework.beans.BeanUtils.copyProperties(source, target);
        } catch (Exception e) {
            logs.error("对象复制失败", e);
        }
    }

    /**
     * @Title clone
     * @Desc Bean属性复制工具方法
     * @Date 2024-03-29 10:50:11
     * @Param source 源对象
     * @Param clazz 目标对象Class
     */
    public static <T> T clone(Object source, Class<T> clazz) {
        T target = null;
        try {
            target = clazz.newInstance();
            clone(source, target);
        } catch (Exception e) {
            logs.error("对象复制失败", e);
        }
        return target;
    }

    /**
     * @return List<T> 指定对象集合
     * @Title clone
     * @Desc 复制集合信息到指定对象
     * @Date 2024-03-29 11:08:17
     * @Param clazz 目标对象Class
     * @Param source 源对象集合
     */
    public static <T> List<T> clone(List<?> source, Class<T> clazz) {
        if (isNull(source)) {
            return Collections.emptyList();
        }
        List<T> results = new ArrayList<>(source.size());
        Constructor<T> constructor = getConstructor(clazz);
        if (constructor == null) {
            return results;
        }
        try {
            for (Object o : source) {
                T t = constructor.newInstance();
                clone(t, o);
                results.add(t);
            }
        } catch (Exception e) {
            logs.error("集合克隆失败", e);
        }
        return results;
    }

    /**
     * @return Constructor<T> 对象构造方法
     * @Title getConstructor
     * @Desc 获取对象构造方法
     * @Date 2024-03-29 11:24:25
     * @Param clazz 对象Class
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getConstructor(Class<T> clazz) {
        String key = clazz.toString();
        if (!CACHE.containsKey(key)) {
            return (Constructor<T>) CACHE.get(key);
        }

        Constructor<T> constructor = null;
        try {
            constructor = clazz.getConstructor();
            CACHE.put(key, constructor);
        } catch (NoSuchMethodException e) {
            logs.error("获取对象构造方法失败", e);
        }
        return constructor;
    }

    /**
     * @return boolean true 对象为空 false 对象不为空
     * @Title isNull
     * @Desc 判断对象是否为空
     * @Date 2024-03-29 10:44:50
     * @Param o 目标对象
     */
    @SuppressWarnings("unchecked")
    public static boolean isNull(Object o) {
        if (o == null) {
            return true;
        }
        // 判断字符是否为空
        if (o instanceof CharSequence) {
            o = o.toString().trim().toLowerCase();
            return "".equals(o) || "null".equals(o);
        }
        // 判断集合是否为空
        if (o instanceof Collection) {
            return ((Collection<?>) o).isEmpty();
        }

        // 判断map是否为空
        if (o instanceof Map) {
            return ((Map<Object, Object>) o).isEmpty();
        }

        // 判断数据类型数组
        if (o.getClass().isArray()) {
            return Array.getLength(o) == 0;
        }

        return false;
    }

    /**
     * @return boolean true 对象不为空 false 对象为空
     * @Title isNotNull
     * @Desc 判断对象是否不为空
     * @Date 2024-03-29 10:44:50
     * @Param o 目标对象
     */
    public static boolean isNotNull(Object o) {
        return !isNull(o);
    }

    /**
     * @return String 不为空返回字符串,为空返回默认值
     * @Title getString
     * @Desc 获取字符串信息, 若对象为空返回”“
     * @Date 2024-03-06 17:55:42
     * @Author HuaAo
     * @Param value 字符信息
     * @Param defaultValue 默认值
     */
    public static String getString(String value, String defaultValue) {
        return isNull(value) ? (defaultValue) : (value.trim());
    }

    /**
     * @return List<Method> 对象的setter方法列表
     * @Title getSetterMethods
     * @Desc 获取对象的setter方法
     * @Date 2024-03-29 11:41:35
     * @Param target 目标对象
     */
    public static List<Method> getSetterMethods(Object target) {
        // setter方法列表
        List<Method> setterMethods = new ArrayList<>();

        // 获取所有方法
        Method[] methods = target.getClass().getMethods();

        // 查找setter方法
        for (Method method : methods) {
            Matcher m = SET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 1)) {
                setterMethods.add(method);
            }
        }
        // 返回setter方法列表
        return setterMethods;
    }

    /**
     * @return List<Method> 对象的getter方法列表
     * @Title getGetterMethods
     * @Desc 获取对象的getter方法
     * @Date 2024-03-29 11:40:51
     * @Param target 目标对象
     */
    public static List<Method> getGetterMethods(Object target) {
        // getter方法列表
        List<Method> getterMethods = new ArrayList<>();
        // 获取所有方法
        Method[] methods = target.getClass().getMethods();
        // 查找getter方法
        for (Method method : methods) {
            Matcher m = GET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 0)) {
                getterMethods.add(method);
            }
        }
        // 返回getter方法列表
        return getterMethods;
    }

    /**
     * @return boolean 属性名一样返回true，否则返回false
     * @Title isMethodPropEquals
     * @Desc 检查Bean方法名中的属性名是否相等。 如getName()和setName()属性名一样，getName()和setAge()属性名不一样。
     * @Date 2024-03-29 11:40:20
     * @Param m1 方法名1
     * @Param m2 方法名2
     */
    public static boolean isMethodPropEquals(String m1, String m2) {
        return m1.substring(BEAN_METHOD_PROP_INDEX).equals(m2.substring(BEAN_METHOD_PROP_INDEX));
    }

    /**
     * @return Field[] 属性信息
     * @Title getFields
     * @Desc 获取类的所有属性, 包括父类
     * @Date 2024-04-17 16:09:43
     * @Param object 对象信息
     */
    public static Field[] getFields(Object object) {
        Class<?> clazz = object.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }


    /**--------------------------------------------------基础数据类型转换------------------------------------------------------**/

    /**
     * @return String 转换结果
     * @Title string
     * @Desc 转换为字符串<br> 如果给定的值为null，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     * @Date 2024-04-12 19:38:36
     * @Param value 被转换的值
     * @Param defaultValue 转换错误时的默认值
     */
    public static String getString(Object value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return value.toString().trim();
    }

    /**
     * 转换为字符<br>
     * 如果给定的值为null，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Character getChar(Object value, Character defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        if (value instanceof Character) {
            return (Character) value;
        }

        final String valueStr = getString(value, null);
        return BeanUtils.isNull(valueStr) ? defaultValue : valueStr.charAt(0);
    }

    /**
     * 转换为字符<br>
     * 如果给定的值为<code>null</code>，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Character getChar(Object value) {
        return getChar(value, null);
    }

    /**
     * 转换为byte<br>
     * 如果给定的值为<code>null</code>，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Byte getByte(Object value, Byte defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Byte) {
            return (Byte) value;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        final String valueStr = getString(value, null);

        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            return Byte.parseByte(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转换为byte<br>
     * 如果给定的值为<code>null</code>，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Byte getByte(Object value) {
        return getByte(value, null);
    }

    /**
     * 转换为Short<br>
     * 如果给定的值为<code>null</code>，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Short getShort(Object value, Short defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Short) {
            return (Short) value;
        }

        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        final String valueStr = getString(value, null);
        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            return Short.parseShort(valueStr.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转换为Short<br>
     * 如果给定的值为<code>null</code>，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Short getShort(Object value) {
        return getShort(value, null);
    }

    /**
     * 转换为int<br>
     * 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Integer getInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        final String valueStr = getString(value, null);

        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(valueStr.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转换为int<br>
     * 如果给定的值为<code>null</code>，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Integer getInteger(Object value) {
        return getInteger(value, null);
    }

    /**
     * 转换为long<br>
     * 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Long getLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        final String valueStr = getString(value, null);
        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            // 支持科学计数法
            return new BigDecimal(valueStr.trim()).longValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转换为long<br>
     * 如果给定的值为<code>null</code>，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Long getLong(Object value) {
        return getLong(value, null);
    }

    /**
     * 转换为double<br>
     * 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Double getDouble(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        final String valueStr = getString(value, null);

        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            // 支持科学计数法
            return new BigDecimal(valueStr.trim()).doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转换为double<br>
     * 如果给定的值为空，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Double getDouble(Object value) {
        return getDouble(value, null);
    }

    /**
     * 转换为Float<br>
     * 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Float getFloat(Object value, Float defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Float) {
            return (Float) value;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        final String valueStr = getString(value, null);
        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(valueStr.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转换为Float<br>
     * 如果给定的值为空，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Float getFloat(Object value) {
        return getFloat(value, null);
    }

    /**
     * 转换为boolean<br>
     * String支持的值为：true、false、yes、ok、no，1,0 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Boolean getBoolean(Object value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        String valueStr = getString(value, null);

        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        valueStr = valueStr.trim().toLowerCase();

        switch (valueStr) {
            case "true":
            case "yes":
            case "ok":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
                return false;
            default:
                return defaultValue;
        }
    }

    /**
     * 转换为boolean<br>
     * 如果给定的值为空，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Boolean getBoolean(Object value) {
        return getBoolean(value, null);
    }

    /**
     * 转换为Number<br>
     * 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Number getNumber(Object value, Number defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number) {
            return (Number) value;
        }

        final String valueStr = getString(value, null);
        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            return NumberFormat.getInstance().parse(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 转换为Number<br>
     * 如果给定的值为空，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static Number getNumber(Object value) {
        return getNumber(value, null);
    }

    /**
     * @return BigInteger 结果
     * @Title getBigInteger
     * @Desc 转换为BigInteger<br> 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     * @Date 2024-04-12 19:34:48
     * @Param value 被转换的值
     * @Param defaultValue 转换错误时的默认值
     */
    public static BigInteger getBigInteger(Object value, BigInteger defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        if (value instanceof Long) {
            return BigInteger.valueOf((Long) value);
        }

        final String valueStr = getString(value, null);

        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigInteger(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * @return BigInteger 结果
     * @Title getBigInteger
     * @Desc 转换为BigInteger<br> 如果给定的值为空，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     * @Date 2024-04-12 19:34:08
     * @Param value 被转换的值
     */
    public static BigInteger getBigInteger(Object value) {
        return getBigInteger(value, null);
    }

    /**
     * @return BigDecimal 结果
     * @Title getDecimal
     * @Desc 转换为BigDecimal<br> 如果给定的值为空，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     * @Date 2024-04-12 19:33:34
     * @Param value 被转换的值
     * @Param defaultValue 转换错误时的默认值
     */
    public static BigDecimal getDecimal(Object value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Long) {
            return new BigDecimal((Long) value);
        }

        if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        }

        if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        }

        final String valueStr = getString(value, null);

        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            return new BigDecimal(valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * @return BigDecimal
     * @Title getBigDecimal
     * @Desc 转换为BigDecimal<br> 如果给定的值为空，或者转换失败，返回0<br>
     * 转换失败不会报错
     * @Date 2024-04-12 19:30:31
     * @Param value
     */
    public static BigDecimal getDecimal(Object value) {
        return getDecimal(value, BigDecimal.ZERO);
    }

    /**-----------------------------------------------数据类型数组转换---------------------------------------------------------**/

    /**
     * 转换为Integer数组<br>
     *
     * @param str 被转换的值
     * @return 结果
     */
    public static Integer[] getIntArray(String str) {
        return getIntArray(",", str);
    }

    /**
     * 转换为Long数组<br>
     *
     * @param str 被转换的值
     * @return 结果
     */
    public static Long[] getLongArray(String str) {
        return getLongArray(",", str);
    }

    /**
     * 转换为Integer数组<br>
     *
     * @param split 分隔符
     * @param str   被转换的值
     * @return 结果
     */
    public static Integer[] getIntArray(String split, String str) {
        if (BeanUtils.isNull(str)) {
            return new Integer[]{};
        }
        String[] arr = str.split(split);
        final Integer[] ints = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            final Integer v = getInteger(arr[i], 0);
            ints[i] = v;
        }
        return ints;
    }

    /**
     * 转换为Long数组<br>
     *
     * @param split 分隔符
     * @param str   被转换的值
     * @return 结果
     */
    public static Long[] getLongArray(String split, String str) {
        if (BeanUtils.isNull(str)) {
            return new Long[]{};
        }
        String[] arr = str.split(split);
        final Long[] longs = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            final Long v = getLong(arr[i], null);
            longs[i] = v;
        }
        return longs;
    }

    /**
     * 转换为String数组<br>
     *
     * @param str 被转换的值
     * @return 结果
     */
    public static String[] getStringArray(String str) {
        return getStringArray(",", str);
    }

    /**
     * 转换为String数组<br>
     *
     * @param split 分隔符
     * @param split 被转换的值
     * @return 结果
     */
    public static String[] getStringArray(String split, String str) {
        return str.split(split);
    }

    /**
     * @return E 转换结果
     * @Title getEnum
     * @Desc 转换为Enum对象<br> 如果给定的值为空，或者转换失败，返回默认值<br>
     * @Date 2024-04-12 19:37:14
     * @Param clazz Enum的Class
     * @Param value 值
     * @Param defaultValue 默认值
     */
    public static <E extends Enum<E>> E getEnum(Class<E> clazz, Object value, E defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (clazz.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            E e = (E) value;
            return e;
        }

        final String valueStr = getString(value, null);
        if (BeanUtils.isNull(valueStr)) {
            return defaultValue;
        }

        try {
            return Enum.valueOf(clazz, valueStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * @return Enum 转换结果
     * @Title getEnum
     * @Desc 转换为Enum对象<br> 如果给定的值为空，或者转换失败，返回默认值<code>null</code><br>
     * @Date 2024-04-12 19:36:00
     * @Param clazz Enum的Class
     * @Param value 值
     */
    public static <E extends Enum<E>> E getEnum(Class<E> clazz, Object value) {
        return getEnum(clazz, value, null);
    }

    /**-----------------------------------------------对象指定字符集转换---------------------------------------------------------**/

    /**
     * @return String 字符串
     * @Title utf8Str
     * @Desc 将对象转为字符串<br> 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
     * 2、对象数组会调用Arrays.getString方法
     * @Date 2024-04-12 19:30:03
     * @Param target 对象
     */
    public static String utf8Str(Object target) {
        return str(target, StandardCharsets.UTF_8);
    }

    /**
     * @return String 字符串
     * @Title str
     * @Desc 将对象转为字符串<br> 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
     * 2、对象数组会调用Arrays.getString方法
     * @Date 2024-04-12 19:27:29
     * @Param target 对象
     * @Param charsetName 字符集名称
     */
    public static String str(Object target, String charsetName) {
        return str(target, Charset.forName(charsetName));
    }

    /**
     * @return String 字符串
     * @Title str
     * @Desc 将对象转为字符串<br> 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
     * 2、对象数组会调用Arrays.getString方法
     * @Date 2024-04-12 18:29:14
     * @Param target 对象
     * @Param charset 字符集
     */
    public static String str(Object target, Charset charset) {
        if (null == target) {
            return null;
        }

        if (target instanceof String) {
            return (String) target;
        }

        if (target instanceof byte[]) {
            return getString((byte[]) target, charset);
        }

        if (target instanceof Byte[]) {
            byte[] bytes = ArrayUtils.toPrimitive((Byte[]) target);
            return getString(bytes, charset);
        }

        if (target instanceof ByteBuffer) {
            return getString((ByteBuffer) target, charset);
        }

        return target.toString();
    }

    /**
     * @return String 字符串
     * @Title getString
     * @Desc 将byte数组转为字符串
     * @Date 2024-04-12 18:28:50
     * @Param bytes byte数组
     * @Param charset 字符集
     */
    public static String getString(byte[] bytes, String charset) {
        return str(bytes, BeanUtils.isNull(charset) ? Charset.defaultCharset() : Charset.forName(charset));
    }

    /**
     * @return String 解码后的字符串
     * @Title getString
     * @Desc 解码字节码
     * @Date 2024-04-12 18:27:07
     * @Param data 字符串
     * @Param charset 字符集
     * 如果此字段为空,则解码的结果取决于平台
     */
    public static String getString(byte[] data, Charset charset) {
        if (data == null) {
            return null;
        }
        return null == charset ? new String(data) : new String(data, charset);
    }

    /**
     * @return String 字符串
     * @Title getString
     * @Desc 将编码的byteBuffer数据转换为字符串
     * @Date 2024-04-12 18:25:50
     * @Param data 数据
     * @Param charset 字符集
     * 如果为空使用当前系统字符集
     */
    public static String getString(ByteBuffer data, String charset) {
        return data == null ? null : str(data, Charset.forName(charset));
    }

    /**
     * @return String 字符串
     * @Title getString
     * @Desc 将编码的byteBuffer数据转换为字符串
     * @Date 2024-04-12 18:24:02
     * @Param data 数据
     * @Param charset 字符集
     * 如果为空使用当前系统字符集
     */
    public static String getString(ByteBuffer data, Charset charset) {
        if (null == charset) {
            charset = Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }

    /**-----------------------------------------------全角半角转换---------------------------------------------------------**/
    /**
     * @return String 全角字符串
     * @Title getSBC
     * @Desc 半角转全角
     * @Date 2024-04-12 18:23:34
     * @Param text 文本
     */
    public static String getSBC(String text) {
        return getSBC(text, null);
    }

    /**
     * @return String 全角字符串
     * @Title getSBC
     * @Desc 半角转全角
     * @Date 2024-04-12 18:22:40
     * @Param text 文本
     * @Param notConvertSet 不替换的字符集合
     */
    public static String getSBC(String text, Set<Character> notConvertSet) {
        char[] c = text.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (null != notConvertSet && notConvertSet.contains(c[i])) {
                // 跳过不替换的字符
                continue;
            }

            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);

            }
        }
        return new String(c);
    }

    /**
     * @return String 半角字符串
     * @Title getDBC
     * @Desc 全角转半角
     * @Date 2024-04-12 18:22:04
     * @Param text 文本
     */
    public static String getDBC(String text) {
        return getDBC(text, null);
    }

    /**
     * @return String 替换后的字符
     * @Title getDBC
     * @Desc 替换全角为半角
     * @Date 2024-04-12 18:21:38
     * @Param text 文本
     * @Param notConvertSet 不替换的字符集合
     */
    public static String getDBC(String text, Set<Character> notConvertSet) {
        char[] c = text.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (null != notConvertSet && notConvertSet.contains(c[i])) {
                // 跳过不替换的字符
                continue;
            }

            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * @return String 中文大写数字
     * @Title digitUppercase
     * @Desc 数字金额大写转换 先写个完整的然后将如零拾替换成零
     * @Date 2024-04-12 18:20:53
     * @Param n 数字
     */
    public static String digitUppercase(double n) {
        String[] fraction = {"角", "分"};
        String[] digit = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[][] unit = {{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};

        String head = n < 0 ? "负" : "";
        n = Math.abs(n);

        String regex = "(零.)+";

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < fraction.length; i++) {
            // 优化double计算精度丢失问题
            BigDecimal nNum = BigDecimal.valueOf(n);
            BigDecimal decimal = new BigDecimal(10);
            BigDecimal scale = nNum.multiply(decimal).setScale(2, RoundingMode.HALF_EVEN);
            double d = scale.doubleValue();
            s.append((digit[(int) (Math.floor(d * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll(regex, ""));
        }

        if (s.length() < 1) {
            s = new StringBuilder("整");
        }

        int integerPart = (int) Math.floor(n);

        for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
            StringBuilder p = new StringBuilder();
            for (int j = 0; j < unit[1].length && n > 0; j++) {
                p.insert(0, digit[integerPart % 10] + unit[1][j]);
                integerPart = integerPart / 10;
            }
            s.insert(0, p.toString().replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i]);
        }
        return head + s.toString().replaceAll("(零.)*零元", "元").replaceFirst(regex, "").replaceAll(regex, "零").replaceAll("^整$", "零元整");
    }

    /**
     * @param source 原值
     * @param target 目标值数组
     * @Title equals
     * @Desc 比较原值是否与目标值数组中对象匹配
     * @Date 2024-06-01 10:11:01.770
     * @Return boolean 匹配到返回true 匹配不到返回false
     */
    public static boolean equals(final Object source, final Object... target) {
        if (source == null) {
            return false;
        }
        for (Object o : target) {
            if (source.equals(o)) {
                return true;
            }
        }
        return false;
    }
}
