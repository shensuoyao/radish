package org.sam.shen.scheduing.sendcloud.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.CollectionUtils;

import org.sam.shen.scheduing.sendcloud.config.Config;
import org.sam.shen.scheduing.sendcloud.exception.ReceiverException;

/**
 * 閭欢鍒楄〃鏀朵欢浜�
 * 
 * @author Sam
 *
 */
public class AddressListReceiver implements Receiver {

	public boolean useAddressList() {
		return true;
	}

	/**
	 * 鍦板潃鍒楄〃
	 */
	private List<String> invokeNames = new ArrayList<String>();

	public List<String> getInvokeNames() {
		return invokeNames;
	}

	/**
	 * 澧炲姞鍦板潃鍒楄〃鐨勮皟鐢ㄥ悕绉�
	 * 
	 * @param to
	 */
	public void addTo(String to) {
		invokeNames.addAll(Arrays.asList(to.split(";")));
	}

	public boolean validate() throws ReceiverException {
		if (CollectionUtils.isEmpty(invokeNames))
			throw new ReceiverException("鍦板潃鍒楄〃涓虹┖");
		if (invokeNames.size() > Config.MAX_MAILLIST)
			throw new ReceiverException("鍦板潃鍒楄〃瓒呰繃涓婇檺");
		return true;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String address : invokeNames) {
			if (sb.length() > 0)
				sb.append(";");
			sb.append(address);
		}
		return sb.toString();
	}
}