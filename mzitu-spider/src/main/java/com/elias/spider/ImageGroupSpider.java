package com.elias.spider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.elias.entity.ImageInfo;
import com.elias.utils.ToolsUtil;

/**
 * 指定index，获取图组中所有图片信息
 * 
 * @author Elias
 * @date 2019年11月28日
 */
@Component
public class ImageGroupSpider {
	private Logger log = LoggerFactory.getLogger(HomePageableSpider.class);

	@Value("${spider.max.error.times}")
	private int max_error_times = 5;
	@Value("${mzitu.baseurl}")
	private String base_url = "https://www.mzitu.com/";

	public List<ImageInfo> getImageUrls(Integer index) {
		List<ImageInfo> list = new ArrayList<ImageInfo>();

		// 根据图组index,拼接图组地址
		String homeUrl = base_url.concat(index.toString());

		// 获取组图的标题和类别
		String desc = getTitle(homeUrl);
		String[] split = desc.split(" - ");
		String title = split[0];
		String group = split[1];

		// 组图的最大页码也就是图片张数
		int maxPage = getMaxPage(homeUrl);

		// 创建图片信息存入集合
		for (Integer page = 1; page <= maxPage; page++) {
			String url = getImageUrl(homeUrl.concat("/").concat(page.toString()), title);
			ImageInfo imageInfo = new ImageInfo(url, index, group, title, page);
			list.add(imageInfo);
		}

		return list;
	}

	// 获取最大页码
	private int getMaxPage(String imageGroupUrl) {
		log.info("getMaxPage 爬取开始，首页地址：{}", imageGroupUrl);
		int maxPage = 0;
		int errorTimes = 0;
		do {
			try {
				Document doc = Jsoup.connect(imageGroupUrl).timeout(5000).userAgent(ToolsUtil.getUserAgent()).get();
				// 解析页面，根据分页页码获取最大页码
				Elements eles = doc.getElementsByClass("pagenavi").get(0).children();
				Element ele = eles.get(eles.size() - 2);
				String text = ele.text();
				maxPage = Integer.valueOf(text);
			} catch (IOException e) {
				errorTimes++;
				maxPage = 0;
				log.error("获取最大页码失败5秒后重试", e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
					log.error(ie.getMessage());
				}
			}
		} while (maxPage == 0 && errorTimes < max_error_times);
		log.info("getMaxPage 爬取结束，最大页码：{}\t首页地址：{}", maxPage, imageGroupUrl);
		return maxPage;
	}

	// 获取标题
	private String getTitle(String imageGroupUrl) {
		log.info("getMaxPage 爬取开始，首页地址：{}", imageGroupUrl);
		String title = null;
		int errorTimes = 0;
		do {
			try {
				Document doc = Jsoup.connect(imageGroupUrl).timeout(5000).userAgent(ToolsUtil.getUserAgent()).get();
				// 解析页面获取页面标题，页面标题格式为 “图组名 - 分类 - 妹子图”
				title = doc.title().replace(" - 妹子图", "");
			} catch (IOException e) {
				errorTimes++;
				title = null;
				log.error("获取最大页码失败5秒后重试", e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
					log.error(ie.getMessage());
				}
			}
		} while (StringUtils.isEmpty(title) && errorTimes < max_error_times); // 获取页面标题成功或者错误次数过多结束
		log.info("getMaxPage 爬取结束，标题：{}\t首页地址：{}", title, imageGroupUrl);
		return title;
	}

	// 获取图片地址
	private String getImageUrl(String imagePageUrl, String title) {
		log.info("getImageUrl 爬取开始，标题：{}\t页面地址：{}", title, imagePageUrl);
		String imageUrl = null;
		int errorTimes = 0;
		do {
			try {
				Document doc = Jsoup.connect(imagePageUrl).timeout(5000).userAgent(ToolsUtil.getUserAgent()).get();
				// 解析页面获取图片地址
				imageUrl = doc.getElementsByAttributeValue("alt", title).attr("src");
			} catch (IOException e) {
				errorTimes++;
				imageUrl = null;
				log.error("获取图片地址失败5秒后重试", e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
					log.error(ie.getMessage());
				}
			}
		} while (StringUtils.isEmpty(imageUrl) && errorTimes < max_error_times);
		log.info("getImageUrl 爬取结束，图片地址：{}\t页面地址：{}", imageUrl, imagePageUrl);
		return imageUrl;
	}
}
