package com.yunji.hygiene.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author : peter-zhu
 * @date : 2024/10/23 9:48
 * @description : TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataResponse<T> extends AbstractResponse<T> {

	private T data;

	public DataResponse() {
	}

	public DataResponse(int code) {
		super.setCode(code);
	}
}
