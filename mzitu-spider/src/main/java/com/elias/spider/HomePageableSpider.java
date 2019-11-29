package com.elias.spider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.elias.utils.ToolsUtil;

/**
 * 根据分页地址获取图组地址
 * 
 * @author Elias
 * @date 2019年11月28日
 */
@Component
public class HomePageableSpider {
	private Logger log = LoggerFactory.getLogger(HomePageableSpider.class);

	@Value("${spider.max.error.times}")
	private int max_error_times = 5;
	@Value("${mzitu.pageable.homeurl}")
	private String home_url = "https://www.mzitu.com/page/1/";

	// 获取指定页的图组地址
	public List<String> getMainImageGroup(String pageUrl) {
		log.info("getMainImageGroup 开始爬取，pageUrl:{}", pageUrl);
		ArrayList<String> list = new ArrayList<String>();
		int errorTimes = 0;
		do {
			try {
				Document doc = Jsoup.connect(pageUrl).timeout(5000).userAgent(ToolsUtil.getUserAgent()).get();
				// 解析返回的页面获取图组第一页，只获取主要的列表，页面其他位置的推荐图组不获取
				Element ele = doc.getElementById("pins");
				Elements lis = ele.children();
				for (Element li : lis) {
					Element a = li.child(0);
					String url = a.attr("href");
					list.add(url);
				}
			} catch (Exception e) {
				errorTimes++;
				list = new ArrayList<String>();
				log.error("爬取失败5秒后重试，网页地址：{}", pageUrl, e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
					log.error(ie.getMessage());
				}
			}
		} while (list.size() == 0 && errorTimes < max_error_times); // 如果获取到结果或者错误次数过多结束
		log.info("getMainImageGroup 爬取结束，结果数：{}\tpageUrl:{}", list.size(), pageUrl);
		return list;
	}

	// 获取最大页码
	public int getMaxPage() {
		log.info("getMaxPage 爬取开始，首页地址：{}", home_url);
		int maxPage = 0;
		// 错误次数
		int errorTimes = 0;
		do {
			try {
				Document doc = Jsoup.connect(home_url).timeout(5000).userAgent(ToolsUtil.getUserAgent()).get();
				// 解析页面，根据分页的页码获取最大页码
				Elements eles = doc.getElementsByClass("page-numbers");
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
		} while (maxPage == 0 && errorTimes < max_error_times);// 如果获取到最大页码或者错误次数过多，结束
		log.info("getMaxPage 爬取结束，最大页码：{}\t首页地址：{}", maxPage, home_url);
		return maxPage;

	}

}
