package com.elias.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ImageInfo {
	// 图片地址
	@NonNull
	private String url;
	// 图组index
	@NonNull
	private Integer index;
	// 图组分类
	@NonNull
	private String group;
	// 图组标题
	@NonNull
	private String title;
	// 图片页码
	@NonNull
	private Integer page;

}
