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

@Component
public class ImageGroupRunner implements CommandLineRunner {
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
		}
	}

}
