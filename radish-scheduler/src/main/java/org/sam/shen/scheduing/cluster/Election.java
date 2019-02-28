package org.sam.shen.scheduing.cluster;

public interface Election {

	/**
	 * 寻找leader主机
	 * @author suoyao
	 * @date 下午6:14:22
	 * @return
	 * @throws InterruptedException
	 */
	public Vote lookForLeader() throws InterruptedException;
	
	/**
	 * 投票结束
	 * @author suoyao
	 * @date 下午5:46:54
	 */
	public void shutdown();
}
