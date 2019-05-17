package org.sam.shen.core.sendcloud.config;

import java.io.InputStream;
import java.util.Properties;

public class Config {

	//
	public static final String CHARSET = "utf-8";
	public static String server = "http://www.sendcloud.net";
	// 普通邮件发送
	public static String send_api = "http://api.sendcloud.net/apiv2/mail/send";
	// 地址列表发送
	public static String send_template_api = "http://api.sendcloud.net/apiv2/mail/sendtemplate";
	// 邮件user
	public static String api_user = null;
	// 邮件key
	public static String api_key = null;

	public static String from = null;
	
	public static String fromName = "Radish 任务抢占系统";
	// 最大收件人数
	public static final int MAX_RECEIVERS = 100;
	// 最大地址列表数
	public static final int MAX_MAILLIST = 5;
	// 邮件内容大小
	public static final int MAX_CONTENT_SIZE = 1024 * 1024;

	static {
		try {
			InputStream f = Config.class.getClassLoader().getResourceAsStream("sendcloud.properties");
			Properties pros = new Properties();
			pros.load(f);
			send_api = pros.getProperty("send_api");
			send_template_api = pros.getProperty("send_template_api");

			api_user = pros.getProperty("api_user");
			api_key = pros.getProperty("api_key");
			from = pros.getProperty("from");
			fromName = pros.getProperty("fromName");
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}