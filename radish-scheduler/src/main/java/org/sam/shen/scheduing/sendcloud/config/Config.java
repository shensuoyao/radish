package org.sam.shen.scheduing.sendcloud.config;

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
	// 邮件发送数据统计(每天)
	public static String send_mail_count_day = "http://api.sendcloud.net/apiv2/statday/list";
	// 邮件发送数据统计(每小时)
	public static String send_mail_count_hour = "http://api.sendcloud.net/apiv2/stathour/list";
	// 无效邮件统计
	public static String send_mail_count_invalid = "http://api.sendcloud.net/apiv2/invalidstat/list";
	// 邮件user
	public static String api_user = null;
	// 邮件key
	public static String api_key = null;

	public static String from = null;
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

			send_mail_count_day = pros.getProperty("send_mail_count_day");
			send_mail_count_hour = pros.getProperty("send_mail_count_hour");
			send_mail_count_invalid = pros.getProperty("send_mail_count_invalid");

			api_user = pros.getProperty("api_user");
			api_key = pros.getProperty("api_key");
			from = pros.getProperty("from");
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}