package org.sam.shen.core.sendcloud.builder;


import org.sam.shen.core.sendcloud.config.Config;
import org.sam.shen.core.sendcloud.core.SendCloud;

public class SendCloudBuilder {

	public static SendCloud build() {
		SendCloud sc = new SendCloud();
		sc.setServer(Config.server);
		sc.setMailAPI(Config.getSendApi());
		sc.setTemplateAPI(Config.getSendTemplateApi());
		return sc;
	}
}