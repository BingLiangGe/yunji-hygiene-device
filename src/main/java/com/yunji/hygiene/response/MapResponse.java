package com.yunji.hygiene.response;

import java.util.Map;

/**
 * @author : peter-zhu
 * @date : 2024/10/23 9:48
 * @description : TODO
 */
public class MapResponse<K, V> extends AbstractResponse<Map<K, V>> {

    private Map<K, V> data;

    @Override
    public Map<K, V> getData() {
        return data;
    }

    @Override
    public void setData(Map<K, V> data) {
        this.data = data;
    }
}
