package com.elias.enums;

import lombok.Getter;

@Getter
public enum ResultEnum {
	PARAM_ERROR(101, "初始化保存路径失败"), 
	PARAM_REPEAT(102, "重复保存"), 
	SPIDER_URI_ERROR(103, "爬虫接口地址错误"), 
	RESULT_NULL(104, "未找到对应数据"),
	TIME_FORMAT_ERROR(105, "日期格式错误");

	private Integer code;
	private String message;

	private ResultEnum(Integer code, String message) {
		this.code = code;
		this.message = message;

	}

	private ResultEnum() {
	}
}
