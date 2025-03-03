package com.yunji.hygiene.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author : peter-zhu
 * @date : 2024/10/23 9:53
 * @description : TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PageResponse<E> extends ListResponse<E> implements Response<List<E>> {
	private Long total;
}
