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
import com.elias.spider.HomePageableSpider;
import com.elias.spider.ImageGroupSpider;
import com.elias.utils.FileUtil;

/**
 * 系统启动后自动爬取分页图组
 * 
 * @author Elias
 * @date 2019年11月29日
 */
@Component
public class HomePageableRunner implements CommandLineRunner {
	@Value("${image.save.path}")
	private String save_path = "D:/mzitu/";
	@Value("${spider.max.page}")
	private Integer max_page = 236;
	@Value("${spider.min.page}")
	private Integer min_page = 1;
	@Value("${mzitu.baseurl}")
	private String base_url = "https://www.mzitu.com/";
	@Value("${home.pageable.spider}")
	private boolean isSpider = false;
	@Value("${spider.threadpool.size}")
	private Integer pool_size = 10;
	
	@Autowired
	private HomePageableSpider hpSpider;
	@Autowired
	private ImageGroupSpider igSpider;
	@Autowired
	private DownloadSpider dlSpider;
	
	private Logger log = LoggerFactory.getLogger(HomePageableRunner.class);

	@Override
	public void run(String... args) {
		if (!isSpider) {
			return;
		}
		log.info("HomePageableRunner begin");

		// 初始化保存图片的文件夹
		try {
			FileUtil.initSavePath(save_path);
		} catch (Exception e1) {
			log.error("HomePageableRunner 初始化文件夹失败：{} ", e1.getMessage());
			return;
		}

		// 初始化线程池
		int poolSize = pool_size < 1 ? 10 : pool_size;
		ExecutorService executorService = Executors.newFixedThreadPool(poolSize);

		// 首末页
		Integer maxPage = max_page < 1 ? hpSpider.getMaxPage() : max_page;
		Integer minPage = min_page < 1 || min_page > maxPage ? 1 : min_page;

		for (; minPage <= maxPage; minPage++) { // 循环读取分页图组列表
			String pageUrl = base_url.concat("page/").concat(minPage.toString());
			List<String> list = hpSpider.getMainImageGroup(pageUrl); // 获取当前页图组地址列表
			if (list != null && list.size() > 0) {
				for (String imageGroupUrl : list) { // 循环图组列表
					// 根据图组地址获取图组index
					String indexStr = imageGroupUrl.substring(imageGroupUrl.lastIndexOf("/") + 1);
					Integer index = Integer.valueOf(indexStr);
					// 获取图组的图片信息列表
					List<ImageInfo> list2 = igSpider.getImageUrls(index);
					// 启动线程爬取图组，下载图片
					executorService.execute(new Runnable() {
						@Override
						public void run() {
							// 下载图片
							if (list2 != null && list2.size() > 0) {
								for (ImageInfo info : list2) {
									dlSpider.downloadImage(info);
								}
							}
						}
					});
				}
			}
		}
	}
}
