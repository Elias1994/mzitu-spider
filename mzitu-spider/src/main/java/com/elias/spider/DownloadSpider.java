package com.elias.spider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.elias.entity.ImageInfo;
import com.elias.utils.FileUtil;

/**
 * 根据图片信息下载图片
 * 
 * @author Elias
 * @date 2019年11月28日
 */
@Component
public class DownloadSpider {
	private Logger log = LoggerFactory.getLogger(DownloadSpider.class);
	@Value("${image.save.path}")
	private String save_path = "D:/mzitu/";
	@Value("${mzitu.baseurl}")
	private String base_url = "https://www.mzitu.com/";
	@Value("${spider.max.error.times}")
	private int max_error_times = 5;

	// 下载图片，保存路径为“配置的基本路径/分类/index_标题/页码.jpg”
	public void downloadImage(ImageInfo info) {
		log.info("下载图片开始：{}", info);
		if (info == null) {
			log.error("ImageInfo is null");
			return;
		}
		// 获取图片所属页面作为Referer
		String referer = base_url.concat(info.getIndex().toString()).concat("/").concat(info.getPage().toString());

		// 获取图片格式后缀
		String imageSuffix = info.getUrl().substring(info.getUrl().lastIndexOf("."));

		// 图片保存文件夹路径
		String imageSavePath = save_path.concat(info.getGroup().concat("/")).concat(info.getIndex() + "-")
				.concat(info.getTitle().concat("/"));
		// 创建文件夹
		try {
			FileUtil.initSavePath(imageSavePath);
		} catch (Exception e1) {
			log.error("downloadImage 初始化文件夹失败：{} ", e1.getMessage());
			return;
		}

		// 图片路径
		String imagePath = imageSavePath.concat(info.getPage().toString()).concat(imageSuffix);

		int errorTimes = 0;
		do {
			try {
				FileUtil.download(info.getUrl(), imagePath, referer);
				log.info("下载图片结束：{}", info);
				return;
			} catch (Exception e) {
				errorTimes++;
				log.error("下载图片失败5秒后重试", e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
					log.error(ie.getMessage());
				}
			}

		} while (errorTimes < max_error_times);
	}

}
