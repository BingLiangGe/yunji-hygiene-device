/**
 * STARPOST CONFIDENTIAL
 * _____________________
 * <p>
 * [2014] - [2016] StarPost Supply Chain Management Co. (Shenzhen) Ltd.
 * All Rights Reserved.
 * <p>
 * NOTICE: All information contained herein is, and remains the property of
 * StarPost Supply Chain Management Co. (Shenzhen) Ltd. and its suppliers, if
 * any. The intellectual and technical concepts contained herein are proprietary
 * to StarPost Supply Chain Management Co. (Shenzhen) Ltd. and its suppliers and
 * may be covered by China and Foreign Patents, patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from StarPost Supply Chain Management Co. (Shenzhen)
 * Ltd.
 */
package com.yunji.hygiene.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json工具类：封装jackson
 *
 * @author zhuzefeng
 *
 */
public class JsonUtil {

    private static final ObjectMapper objectMapper;

    // 不准改objectMapper 属性。。如果需要修改 自己NEW一个objectMapper。。否则会造成BUG by peter-zhu
    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public static <T> T readValue(String jsonStr, Class<T> valueType) {
    	try {
    		return objectMapper.readValue(jsonStr, valueType);
		} catch (Exception e) {
			return null;
		}
    }

    /**
     * 转换泛型对象
     *
     * @param jsonStr
     * @param type
     * @return
     */
    public static <T> T readValue(String jsonStr, TypeReference<T> type) throws JsonProcessingException, IOException {
        return objectMapper.readValue(jsonStr, type);
    }

    /**
     * 根据流获取指定对象
     *
     * @param valueType
     * @return
     */
    public static <T> T readValue(InputStream in, Class<T> valueType) throws JsonProcessingException, IOException {
        return readValue(objectMapper.readTree(in), valueType);
    }

    /**
     * 根据Json节点获取指定对象
     *
     * @param valueType
     * @return
     */
    public static <T> T readValue(JsonNode node, Class<T> valueType) throws JsonProcessingException, IOException {
        return objectMapper.convertValue(node, valueType);
    }

    /**
     * 把json字符串转成json节点对象
     *
     * @param jsonStr
     * @return JsonNode
     * @throws IOException
     * @throws JsonProcessingException
     */
    public static JsonNode getJsonNode(String jsonStr) throws JsonProcessingException, IOException {
        return objectMapper.readTree(jsonStr);
    }

    /**
     * 把指定对象转换为json字符串
     *
     * @param object
     * @return
     */
    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 传入对象集合的jsonString,转为 List对象
     *
     * @param jsonString
     * @param cls
     * @return
     */
    public static <T> List<T> convertList(String jsonString, Class<T> cls) throws JsonProcessingException, IOException {
        JavaType javaType = getCollectionType(ArrayList.class, cls);
        return objectMapper.readValue(jsonString, javaType);
    }

    public static <K, V> Map<K, V> convertMap(String jsonString, Class<K> kCls, Class<V> vCls)
            throws JsonParseException, JsonMappingException, IOException {
        JavaType javaType = getCollectionType(HashMap.class, kCls, vCls);
        return objectMapper.readValue(jsonString, javaType);
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}
