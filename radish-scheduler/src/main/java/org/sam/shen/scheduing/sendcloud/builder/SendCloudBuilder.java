package org.sam.shen.scheduing.sendcloud.builder;

import org.sam.shen.scheduing.sendcloud.config.Config;
import org.sam.shen.scheduing.sendcloud.core.SendCloud;

public class SendCloudBuilder {

	public static SendCloud build() {
		SendCloud sc = new SendCloud();
		sc.setServer(Config.server);
		sc.setMailAPI(Config.send_api);
		sc.setTemplateAPI(Config.send_template_api);
		return sc;
	}
}