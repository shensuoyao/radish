package org.sam.shen.core.constants;

/**
 * @author suoyao
 * @date 2018年8月1日 下午3:30:03
  * 常量类
 */
public class Constant {

	public static final int BEAT_TIMEOUT = 5;
	
	public static final int SUCCESS_EXIT = 0;
	
	public static final String SPLIT_CHARACTER = "-";
	
	public static final String SPLIT_CHARACTER2 = ",";
	
	public static final String SPLIT_CHARACTER_ARROW = "->";
	
	public static final String HTTP_PREFIX = "http://";
	
	public static final String AGENT_CONTEXT_PATH = "/radish-agent/handler-log";
	
	public static final int YES = 1;
	
	public static final int NO = 0;
	
	public static final String REDIS_AGENT_PREFIX = "agent_";
	
	public static final String REDIS_EVENT_PREFIX = "event_";

	public static final String DEFAULT_LOG_FILE_PATH_LINUX = "/tmp/log/radish";

	public static final String DEFAULT_LOG_FILE_PATH_WIN = "C:\\radish\\log";

    public static final String DEFAULT_SHELL_SCRIPT_FILE_PATH = "/tmp/log/radish";

    public static final String SHELL_SCRIPT_PATH = "/shell";

	public static final String SHELL_SCRIPT_NAME = "monitor.sh";

	public static final String CHECK_SHELL_SCRIPT_NAME = "check-sysstat.sh";

	public static final int DEFAULT_AGENT_PORT = 8083;

	public static final int DEFAULT_NETTY_PORT = 8084;

	public static final String AGENT_LOG_NETWORK_NETTY = "netty";

	public static final String AGENT_LOG_NETWORK_SERVLET = "servlet";
	
}
