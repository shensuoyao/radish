package org.sam.shen.scheduing.sendcloud;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.scheduing.sendcloud.builder.SendCloudBuilder;
import org.sam.shen.scheduing.sendcloud.config.Config;
import org.sam.shen.scheduing.sendcloud.core.SendCloud;
import org.sam.shen.scheduing.sendcloud.model.MailAddressReceiver;
import org.sam.shen.scheduing.sendcloud.model.MailBody;
import org.sam.shen.scheduing.sendcloud.model.SendCloudMail;
import org.sam.shen.scheduing.sendcloud.model.TextContent;
import org.sam.shen.scheduing.sendcloud.model.TextContent.ScContentType;
import org.sam.shen.scheduing.sendcloud.util.ResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *  发送邮件客户端
 * @author suoyao
 * @date 2018年9月10日 下午3:03:36
  * 
 */
public class SendEmailClient {
	final static Logger logger = LoggerFactory.getLogger(SendEmailClient.class);

	/**
	 * 发送邮件
	 * 
	 * @param emailparm
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static boolean sendEmail(String emailparm, String title, String content, String cc) throws Exception {
		// 发送邮件
		MailBody body = new MailBody();
		body.setFrom(Config.from);
		// 设置 FromName
		body.setFromName(Config.fromName);
		// 设置 ReplyTo
		body.setReplyTo("");
		// 设置标题
		body.setSubject(title);
		MailAddressReceiver receiver = null;
		receiver = new MailAddressReceiver();
		String[] emails = emailparm.split(",");
		StringBuilder sb = new StringBuilder();
		String email = "";
		for (String sto : emails) {
			email = sto.replaceAll("\\s*", "");
			sb.append(email + ";");
		}
		email = sb.toString().substring(0, sb.toString().lastIndexOf(";"));
		if (StringUtils.isBlank(email)) {
			return false;
		}
		// 添加收件人
		receiver.addTo(email);
		// 添加抄送
		if(StringUtils.isNotBlank(cc)) {
			receiver.addCc(cc);
		}
		// 添加密送
		/*if (StringUtils.isNotBlank(cc)) {
			receiver.addBcc(cc);
		}*/
		SendCloudMail mail = new SendCloudMail();
		mail.setBody(body);
		TextContent txContent = new TextContent();
		txContent.setContent_type(ScContentType.html);
		txContent.setText(content);

		mail.setContent(txContent);
		mail.setTo(receiver);
		ResponseData res = null;
		try {
			SendCloud sc = SendCloudBuilder.build();
			res = sc.sendMail(mail);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return (res.getStatusCode() == 200);
	}

}