package com.yunji.hygiene.response;


import java.util.List;
/**
 * @author : peter-zhu
 * @date : 2024/10/23 9:53
 * @description : TODO
 */
public class ListResponse<E> extends AbstractResponse<List<E>> {

    private List<E> data;

	@Override
	public List<E> getData() {
		return data;
	}

	@Override
	public void setData(List<E> data) {
		this.data = data;
	}
}
