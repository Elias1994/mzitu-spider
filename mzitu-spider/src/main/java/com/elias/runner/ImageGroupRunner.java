package com.elias.runner;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.elias.entity.ImageInfo;
import com.elias.spider.DownloadSpider;
import com.elias.spider.ImageGroupSpider;
import com.elias.utils.FileUtil;

/**
 * 启动时自动运行该类的run方法，爬取配置指定index的组图
 * 
 * @author Elias
 * @date 2019年11月29日
 */
@Component
public class ImageGroupRunner implements CommandLineRunner {
	@Value("${image.save.path}")
	private String save_path = "D:/mzitu/";
	@Value("${mzitu.baseurl}")
	private String base_url = "https://www.mzitu.com/";
	@Value("${image.group.spider}")
	private boolean isSpider = false;
	@Value("${image.group.index}")
	private Integer index = 10;
	@Value("${spider.threadpool.size}")
	private Integer pool_size = 10;

	@Autowired
	private ImageGroupSpider igSpider;
	@Autowired
	private DownloadSpider dlSpider;

	private Logger log = LoggerFactory.getLogger(ImageGroupRunner.class);

	@Override
	public void run(String... args) throws Exception {
		if (!isSpider) {
			return;
		}
		if (index == null || index < 1) {
			log.error("非法的index:{}", index);
			return;
		}

		// 初始化保存图片的文件夹
		try {
			FileUtil.initSavePath(save_path);
		} catch (Exception e1) {
			log.error("ImageGroupRunner 初始化文件夹失败：{} ", e1.getMessage());
			return;
		}

		// 获取图片信息
		List<ImageInfo> list2 = igSpider.getImageUrls(index);

		int poolSize = pool_size < 1 ? 10 : pool_size;
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
		// 下载图片
		if (list2 != null && list2.size() > 0) {
			for (ImageInfo info : list2) {
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						dlSpider.downloadImage(info);
					}
				});

			}
		} else {
			log.error("index为{}的图组获取图片信息失败！", index);
		}
	}

}
