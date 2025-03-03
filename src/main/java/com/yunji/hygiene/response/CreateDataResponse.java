package com.yunji.hygiene.response;

import lombok.Data;

import java.util.List;

/**
 * @author : peter-zhu
 * @date : 2024/12/20 16:00
 * @description : TODO
 **/
@Data
public class CreateDataResponse<T> {

    private List<T> dataIds;
}
