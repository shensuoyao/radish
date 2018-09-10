package org.sam.shen.scheduing.sendcloud.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.Asserts;
import org.apache.http.util.EntityUtils;

import org.sam.shen.scheduing.sendcloud.config.Config;
import org.sam.shen.scheduing.sendcloud.config.Credential;
import org.sam.shen.scheduing.sendcloud.model.MailAddressReceiver;
import org.sam.shen.scheduing.sendcloud.model.SendCloudMail;
import org.sam.shen.scheduing.sendcloud.model.SendCloudMailCount;
import org.sam.shen.scheduing.sendcloud.model.TemplateContent;
import org.sam.shen.scheduing.sendcloud.model.TextContent;
import org.sam.shen.scheduing.sendcloud.model.TextContent.ScContentType;
import org.sam.shen.scheduing.sendcloud.util.ResponseData;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * 发送邮件代码示例
 * <p>
 * <blockquote>
 * 
 * <pre>
 * SendCloud sc = SendCloudBuilder.build();
 * 
 * // 创建邮件body
 * MailBody body = new MailBody();
 * body.setFrom("test@163.com");
 * body.setFromName("张三");
 * body.setReplyTo("service@qq.com");
 * body.setSubject("测试");
 * // 创建文件附件
 * body.addAttachments(new File("D:/test.txt"));
 * // 创建流附件
 * body.addAttachments(new FileInputStream(new File("D:/ff.png")));
 * // 邮箱收件人
 * MailAddressReceiver receiver = new MailAddressReceiver();
 * receiver.setBroadcastSend(true);// 广播发送(收件人会全部显示)
 * receiver.addTo("1234@qq.com");
 * 
 * // 地址列表收件人
 * // MailListReceiver receiver=new MailListReceiver();
 * // 添加邮件地址列表
 * // receiver.addMailList("developers@sendcloud.com");
 * 
 * // 创建模版邮件内容
 * TemplateContent content = new TemplateContent();
 * content.setTemplateInvokeName("templateInvokeName");
 * 
 * // 创建文本邮件内容
 * // TextContent content = new TextContent();
 * // content.setContent_type(ScContentType.html);
 * // content.setText("hello world");
 * 
 * // 创建邮件
 * SendCloudMail scmail = new SendCloudMail();
 * scmail.setBody(body);
 * scmail.setContent(content);
 * scmail.setTo(receiver);
 * 
 * // 发信
 * ResponseData result = sc.sendMail(scmail);
 * System.out.println(JSONObject.fromObject(result).toString());
 * </pre>
 * 
 * </blockquote>
 * <p>
 * 
 * 发送短信代码示例
 * 
 * 
 * <p>
 * <blockquote>
 * 
 * <pre>
 * SendCloud sc = SendCloudBuilder.build();
 * 
 * SendCloudSms sms = new SendCloudSms();
 * sms.setTemplateId(65825);
 * sms.addPhone("13512345678");
 * sms.addVars("code", "123456");
 * 
 * ResponseData result = sc.sendSms(sms);
 * 
 * System.out.println(JSONObject.fromObject(result).toString());
 * </pre>
 * 
 * </blockquote>
 * <p>
 * 
 * 发送语音代码示例
 * <p>
 * <blockquote>
 * 
 * <pre>
 * SendCloud sc = SendCloudBuilder.build();
 * 
 * SendCloudVoice sms = new SendCloudVoice();
 * sms.setPhone("13312345678");
 * sms.setCode("1234");
 * 
 * ResponseData result = sc.sendVoice(sms);
 * 
 * System.out.println(JSONObject.fromObject(result).toString());
 * </pre>
 * 
 * </blockquote>
 * <p>
 * 
 * @author Sam
 *
 */
public class SendCloud {

	private String server;
	private String mailAPI;
	private String templateAPI;
	private String mailCountDayAPI;
	private String mailCountHourAPI;
	private String invalidMailCountAPI;

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getMailAPI() {
		return mailAPI;
	}

	public void setMailAPI(String mailAPI) {
		this.mailAPI = mailAPI;
	}

	public String getTemplateAPI() {
		return templateAPI;
	}

	public void setTemplateAPI(String templateAPI) {
		this.templateAPI = templateAPI;
	}

	public String getMailCountDayAPI() {
		return mailCountDayAPI;
	}

	public void setMailCountDayAPI(String mailCountDayAPI) {
		this.mailCountDayAPI = mailCountDayAPI;
	}

	public String getMailCountHourAPI() {
		return mailCountHourAPI;
	}

	public void setMailCountHourAPI(String mailCountHourAPI) {
		this.mailCountHourAPI = mailCountHourAPI;
	}

	public String getInvalidMailCountAPI() {
		return invalidMailCountAPI;
	}

	public void setInvalidMailCountAPI(String invalidMailCountAPI) {
		this.invalidMailCountAPI = invalidMailCountAPI;
	}

	/**
	 * 发送邮件
	 * 
	 * @param credential
	 *            身份认证
	 * @param mail
	 *            邮件
	 */
	public ResponseData sendMail(SendCloudMail mail) throws Throwable {
		Asserts.notNull(mail, "mail");
		Asserts.notBlank(Config.api_user, "api_user");
		Asserts.notBlank(Config.api_key, "api_key");
		mail.validate();
		Credential credential = new Credential(Config.api_user, Config.api_key);
		if (CollectionUtils.isEmpty(mail.getBody().getAttachments())) {
			return post(credential, mail);
		} else {
			return multipartPost(credential, mail);
		}
	}

	/**
	 * 普通方式发送
	 * 
	 * @param credential
	 * @param mail
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private ResponseData post(Credential credential, SendCloudMail mail) throws ClientProtocolException, IOException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("apiUser", credential.getApiUser()));
		params.add(new BasicNameValuePair("apiKey", credential.getApiKey()));
		params.add(new BasicNameValuePair("from", mail.getBody().getFrom()));
		params.add(new BasicNameValuePair("fromname", mail.getBody().getFromName()));
		params.add(new BasicNameValuePair("subject", mail.getBody().getSubject()));
		params.add(new BasicNameValuePair("replyTo", mail.getBody().getReplyTo()));
		if (mail.getBody().getLabelId() != null)
			params.add(new BasicNameValuePair("labelId", mail.getBody().getLabelId().toString()));

		/**
		 * 是否使用模版发送
		 */
		if (mail.getContent().useTemplate()) {
			TemplateContent content = (TemplateContent) mail.getContent();
			params.add(new BasicNameValuePair("templateInvokeName", content.getTemplateInvokeName()));
		} else {
			TextContent content = (TextContent) mail.getContent();
			if (content.getContent_type().equals(ScContentType.html)) {
				params.add(new BasicNameValuePair("html", content.getText()));
			} else {
				params.add(new BasicNameValuePair("plain", content.getText()));
			}
		}
		/**
		 * 是否使用地址列表
		 */
		if (mail.getTo() != null) {
			if (mail.getTo().useAddressList()) {
				params.add(new BasicNameValuePair("useAddressList", "true"));
				params.add(new BasicNameValuePair("to", mail.getTo().toString()));
			} else {
				MailAddressReceiver receiver = (MailAddressReceiver) mail.getTo();
				if (!mail.getContent().useTemplate() && receiver.isBroadcastSend()) {
					params.add(new BasicNameValuePair("to", receiver.toString()));
					params.add(new BasicNameValuePair("cc", receiver.getCcString()));
					params.add(new BasicNameValuePair("bcc", receiver.getBccString()));
				} else {
					if (mail.getBody().getXsmtpapi() != null && !mail.getBody().getXsmtpapi().containsKey("to")) {
						mail.getBody().addXsmtpapi("to", JSON.toJSONString(receiver.getTo()));
					}
				}
			}
		}
		if (MapUtils.isNotEmpty(mail.getBody().getHeaders()))
			params.add(new BasicNameValuePair("headers", mail.getBody().getHeadersString()));
		if (MapUtils.isNotEmpty(mail.getBody().getXsmtpapi()))
			params.add(new BasicNameValuePair("xsmtpapi", mail.getBody().getXsmtpapiString()));
		params.add(new BasicNameValuePair("respEmailId", "true"));
		params.add(new BasicNameValuePair("useNotification", "false"));

		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(mail.getContent().useTemplate() ? templateAPI : mailAPI);
		httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		HttpResponse response = httpclient.execute(httpPost);
		ResponseData result = validate(response);
		httpPost.releaseConnection();
		httpclient.close();
		return result;
	}

	/**
	 * multipart方式发送
	 * 
	 * @param credential
	 * @param mail
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private ResponseData multipartPost(Credential credential, SendCloudMail mail)
			throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(mail.getContent().useTemplate() ? templateAPI : mailAPI);
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		MultipartEntityBuilder entity = MultipartEntityBuilder.create();
		entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.setCharset(Charset.forName("UTF-8"));
		ContentType TEXT_PLAIN = ContentType.create("text/plain", Charset.forName("UTF-8"));
		entity.addTextBody("apiUser", credential.getApiUser(), TEXT_PLAIN);
		entity.addTextBody("apiKey", credential.getApiKey(), TEXT_PLAIN);
		entity.addTextBody("from", mail.getBody().getFrom(), TEXT_PLAIN);
		if (StringUtils.isNotEmpty(mail.getBody().getFromName()))
			entity.addTextBody("fromname", mail.getBody().getFromName(), TEXT_PLAIN);
		entity.addTextBody("subject", mail.getBody().getSubject(), TEXT_PLAIN);
		if (StringUtils.isNotEmpty(mail.getBody().getReplyTo()))
			entity.addTextBody("replyTo", mail.getBody().getReplyTo(), TEXT_PLAIN);
		if (mail.getBody().getLabelId() != null)
			entity.addTextBody("labelId", mail.getBody().getLabelId().toString(), TEXT_PLAIN);
		/**
		 * 是否使用模版发送
		 */
		if (mail.getContent().useTemplate()) {
			TemplateContent content = (TemplateContent) mail.getContent();
			entity.addTextBody("templateInvokeName", content.getTemplateInvokeName(), TEXT_PLAIN);
		} else {
			TextContent content = (TextContent) mail.getContent();
			if (content.getContent_type().equals(ScContentType.html)) {
				entity.addTextBody("html", content.getText(), TEXT_PLAIN);
			} else {
				entity.addTextBody("plain", content.getText(), TEXT_PLAIN);
			}
		}
		/**
		 * 是否使用地址列表
		 */
		if (mail.getTo() != null) {
			if (mail.getTo().useAddressList()) {
				entity.addTextBody("useAddressList", "true", TEXT_PLAIN);
				entity.addTextBody("to", mail.getTo().toString(), TEXT_PLAIN);
			} else {
				MailAddressReceiver receiver = (MailAddressReceiver) mail.getTo();

				if (!mail.getContent().useTemplate() && receiver.isBroadcastSend()) {
					entity.addTextBody("to", receiver.toString(), TEXT_PLAIN);
					if (StringUtils.isNotEmpty(receiver.getCcString()))
						entity.addTextBody("cc", receiver.getCcString(), TEXT_PLAIN);
					if (StringUtils.isNotEmpty(receiver.getBccString()))
						entity.addTextBody("bcc", receiver.getBccString(), TEXT_PLAIN);
				} else {
					if (mail.getBody().getXsmtpapi() == null || !mail.getBody().getXsmtpapi().containsKey("to")) {
						mail.getBody().addXsmtpapi("to", JSON.toJSONString(receiver.getTo()));
					}
				}
			}
		}
		if (MapUtils.isNotEmpty(mail.getBody().getHeaders()))
			entity.addTextBody("headers", mail.getBody().getHeadersString(), TEXT_PLAIN);
		if (MapUtils.isNotEmpty(mail.getBody().getXsmtpapi()))
			entity.addTextBody("xsmtpapi", mail.getBody().getXsmtpapiString(), TEXT_PLAIN);
		entity.addTextBody("respEmailId", "true", TEXT_PLAIN);
		entity.addTextBody("useNotification", "false", TEXT_PLAIN);

		ContentType OCTEC_STREAM = ContentType.create("application/octet-stream", Charset.forName("UTF-8"));
		for (Object o : mail.getBody().getAttachments()) {
			if (o instanceof File) {
				entity.addBinaryBody("attachments", (File) o, OCTEC_STREAM, ((File) o).getName());
			} else {
				entity.addBinaryBody("attachments", (InputStream) o, OCTEC_STREAM, UUID.randomUUID().toString());
			}
		}
		httpPost.setEntity(entity.build());
		HttpResponse response = httpclient.execute(httpPost);
		ResponseData result = validate(response);
		httpPost.releaseConnection();
		httpclient.close();
		return result;
	}

	/**
	 * 解析返回结果
	 * 
	 * @param response
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */

	private static ObjectMapper mapper;

	private ResponseData validate(HttpResponse response) throws ParseException, IOException {
		String s = EntityUtils.toString(response.getEntity());
		ResponseData result = new ResponseData();
		if (StringUtils.isNotBlank(s)) {
			mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(s);
			if (jsonNode.has("statusCode")) {
				result.setStatusCode(jsonNode.get("statusCode").asInt());
				result.setMessage(jsonNode.get("message").asText());
				result.setResult(jsonNode.get("result").asBoolean());
				result.setInfo(jsonNode.get("info").toString());
			} else {
				result.setStatusCode(500);
				result.setMessage(jsonNode.toString());
			}
		} else {
			result.setStatusCode(response.getStatusLine().getStatusCode());
			result.setMessage("发送失败");
			result.setResult(false);
		}
		/*
		 * if (JSONUtils.mayBeJSON(s)) { JSONObject json = JSONObject.fromObject(s); if
		 * (json.containsKey("statusCode")) {
		 * result.setStatusCode(json.getInt("statusCode"));
		 * result.setMessage(json.getString("message"));
		 * result.setResult(json.getBoolean("result"));
		 * result.setInfo(json.getJSONObject("info").toString()); } else {
		 * result.setStatusCode(500); result.setMessage(json.toString()); } } else {
		 * result.setStatusCode(response.getStatusLine().getStatusCode());
		 * result.setMessage("发送失败"); result.setResult(false); }
		 */
		return result;
	}

	/**
	 * 邮件发送数据统计
	 * <p>
	 * 支持按天统计和按小时统计
	 * </p>
	 * 
	 * @param sendCloudMailCount
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public ResponseData mailCount(SendCloudMailCount sendCloudMailCount) throws ClientProtocolException, IOException {
		Asserts.notNull(sendCloudMailCount, "sendCloudMailCount");
		Asserts.notBlank(Config.api_user, "api_user");
		Asserts.notBlank(Config.api_key, "api_key");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Credential credential = new Credential(Config.api_user, Config.api_key);
		params.add(new BasicNameValuePair("apiUser", credential.getApiUser()));
		params.add(new BasicNameValuePair("apiKey", credential.getApiKey()));

		String uri = "";
		if (sendCloudMailCount.getDays() > 0) {
			// 按每天统计
			params.add(new BasicNameValuePair("days", String.valueOf(sendCloudMailCount.getDays())));
			uri = mailCountDayAPI;
		} else {
			// 按每小时统计
			uri = mailCountHourAPI;
		}
		if (StringUtils.isNotEmpty(sendCloudMailCount.getStartDate())
				&& StringUtils.isNotEmpty(sendCloudMailCount.getEndDate())) {
			// 按每小时统计
			params.add(new BasicNameValuePair("startDate", sendCloudMailCount.getStartDate()));
			params.add(new BasicNameValuePair("endDate", sendCloudMailCount.getEndDate()));
			uri = mailCountHourAPI;
		} else {
			// 按每天统计
			uri = mailCountDayAPI;
		}
		if (sendCloudMailCount.getApiUserList() != null && sendCloudMailCount.getApiUserList().size() > 0) {
			params.add(new BasicNameValuePair("apiUserList", sendCloudMailCount.toApiUserString()));
		}
		if (sendCloudMailCount.getLabelIdList() != null && sendCloudMailCount.getLabelIdList().size() > 0) {
			params.add(new BasicNameValuePair("labelIdList", sendCloudMailCount.toLabelIdString()));
		}
		if (sendCloudMailCount.getDomainList() != null && sendCloudMailCount.getDomainList().size() > 0) {
			params.add(new BasicNameValuePair("domainList", sendCloudMailCount.toDomainString()));
		}
		if (sendCloudMailCount.getAggregate() >= 0) {
			params.add(new BasicNameValuePair("aggregate", String.valueOf(sendCloudMailCount.getAggregate())));
		}
		uri += "?" + EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));

		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(uri);

		HttpResponse response = httpclient.execute(httpGet);
		ResponseData result = validate(response);
		httpGet.releaseConnection();
		httpclient.close();

		return result;
	}

	/**
	 * 无效邮件发送数据统计
	 * <p>
	 * 支持按天统计和按小时统计
	 * </p>
	 * 
	 * @param sendCloudMailCount
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public ResponseData invalidmailCount(SendCloudMailCount sendCloudMailCount)
			throws ClientProtocolException, IOException {
		Asserts.notNull(sendCloudMailCount, "sendCloudMailCount");
		Asserts.notBlank(Config.api_user, "api_user");
		Asserts.notBlank(Config.api_key, "api_key");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Credential credential = new Credential(Config.api_user, Config.api_key);
		params.add(new BasicNameValuePair("apiUser", credential.getApiUser()));
		params.add(new BasicNameValuePair("apiKey", credential.getApiKey()));

		String uri = invalidMailCountAPI;
		if (sendCloudMailCount.getDays() > 0) {
			// 按每天统计
			params.add(new BasicNameValuePair("days", String.valueOf(sendCloudMailCount.getDays())));
		}
		if (StringUtils.isNotEmpty(sendCloudMailCount.getStartDate())
				&& StringUtils.isNotEmpty(sendCloudMailCount.getEndDate())) {
			// 按每小时统计
			params.add(new BasicNameValuePair("startDate", sendCloudMailCount.getStartDate()));
			params.add(new BasicNameValuePair("endDate", sendCloudMailCount.getEndDate()));
		}
		if (sendCloudMailCount.getApiUserList() != null && sendCloudMailCount.getApiUserList().size() > 0) {
			params.add(new BasicNameValuePair("apiUserList", sendCloudMailCount.toApiUserString()));
		}
		if (sendCloudMailCount.getLabelIdList() != null && sendCloudMailCount.getLabelIdList().size() > 0) {
			params.add(new BasicNameValuePair("labelIdList", sendCloudMailCount.toLabelIdString()));
		}
		if (sendCloudMailCount.getDomainList() != null && sendCloudMailCount.getDomainList().size() > 0) {
			params.add(new BasicNameValuePair("domainList", sendCloudMailCount.toDomainString()));
		}
		if (sendCloudMailCount.getAggregate() >= 0) {
			params.add(new BasicNameValuePair("aggregate", String.valueOf(sendCloudMailCount.getAggregate())));
		}
		uri += "?" + EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));

		CloseableHttpClient httpclient = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(uri);

		HttpResponse response = httpclient.execute(httpGet);
		ResponseData result = validate(response);
		httpGet.releaseConnection();
		httpclient.close();

		return result;
	}

}