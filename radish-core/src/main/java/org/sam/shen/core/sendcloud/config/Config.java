package org.sam.shen.core.sendcloud.config;

import java.io.InputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Config {

	//
	public static final String CHARSET = "utf-8";
	public static final String server = "http://www.sendcloud.net";
	// 普通邮件发送
	private static String send_api = "http://api.sendcloud.net/apiv2/mail/send";
	// 地址列表发送
	private static String send_template_api = "http://api.sendcloud.net/apiv2/mail/sendtemplate";
	// 邮件user
	private static String api_user = null;
	// 邮件key
	private static String api_key = null;

	private static String from = null;
	
	private static String fromName = null;
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
			log.error("error:", e);
		}
	}
	
	public static String getSendApi() {
		return send_api;
	}
	
	public static String getSendTemplateApi() {
		return send_template_api;
	}
	
	public static String getApiUser() {
		return api_user;
	}
	
	public static String getApiKey() {
		return api_key;
	}
	
	public static String getFrom() {
		return from;
	}
	
	public static String getFromName() {
		return fromName;
	}
	
}