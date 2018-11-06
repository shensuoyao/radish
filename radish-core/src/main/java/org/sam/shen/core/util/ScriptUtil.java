package org.sam.shen.core.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.model.AgentMonitorInfo;

import java.io.*;
import java.util.*;

/**
  * 1、内嵌编译器如"PythonInterpreter"无法引用扩展包，因此推荐使用java调用控制台进程方式"Runtime.getRuntime().exec()"来运行脚本(shell或python)；
  * 2、因为通过java调用控制台进程方式实现，需要保证目标机器PATH路径正确配置对应编译器；
  * 3、暂时脚本执行日志只能在脚本执行结束后一次性获取，无法保证实时性；因此为确保日志实时性，可改为将脚本打印的日志存储在指定的日志文件上； 4、python
  * 异常输出优先级高于标准输出，体现在Log文件中，因此推荐通过logging方式打日志保持和异常信息一致；否则用prinf日志顺序会错乱
 * @author suoyao
 * @date 2018年8月1日 上午8:15:38
  * 
 */
public class ScriptUtil {

	/**
	 * make script file
	 *
	 * @param scriptFileName
	 * @param content
	 * @throws IOException
	 */
	public static void markScriptFile(String scriptFileName, String content) throws IOException {
		// make file, filePath/gluesource/666-123456789.py
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(scriptFileName);
			fileOutputStream.write(content.getBytes("UTF-8"));
			fileOutputStream.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		}
	}

	/**
	 * 日志文件输出方式
	 *
	 * 优点：支持将目标数据实时输出到指定日志文件中去 缺点： 标准输出和错误输出优先级固定，可能和脚本中顺序不一致 Java无法实时获取
	 *
	 * @param command
	 * @param scriptFile
	 * @param logFile
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static int execToFile(String command, String scriptFile, String logFile, String... params)
	        throws IOException {
		// 标准输出：print （null if watchdog timeout）
		// 错误输出：logging + 异常 （still exists if watchdog timeout）
		// 标准输入

		FileOutputStream fileOutputStream = null; //
		try {
			fileOutputStream = new FileOutputStream(logFile, true);
			PumpStreamHandler streamHandler = new PumpStreamHandler(fileOutputStream, fileOutputStream, null);

			// command
			CommandLine commandline = new CommandLine(command);
			commandline.addArgument(scriptFile);
			if (params != null && params.length > 0) {
				commandline.addArguments(params);
			}

			// exec
			DefaultExecutor exec = new DefaultExecutor();
			exec.setExitValues(null);
			exec.setStreamHandler(streamHandler);
			int exitValue = exec.execute(commandline); // exit code: 0=success, 1=error
			return exitValue;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}


    /**
     * 执行shell脚本
     * @author clock
     * @date 2018/10/31 上午10:05
     * @param command shell命令
     * @param params 传递参数
     * @return shell脚本执行的结果
     */
	public static String execShellCmd(String command, String... params) {
	    Process process = null;
        String cmd = command.concat(" ").concat(StringUtils.join(params, " "));
        try {
            process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmd});
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (process == null) {
            return "";
        }

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ByteArrayOutputStream ebos = new ByteArrayOutputStream();
        final InputStream is = process.getInputStream();
        final InputStream es = process.getErrorStream();
        // 启动执行命令结果输出线程
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = is.read(buffer)) != -1) {
                    bos.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        // 启动执行命令错误流输出线程
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = es.read(buffer)) != -1) {
                    ebos.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

	    try {
            // 等待线程执行完毕
            process.waitFor();
            String msg = bos.toString("utf-8").trim();
            String errMsg = ebos.toString("utf-8").trim();
            if (StringUtils.isNotEmpty(msg)) {
                return msg;
            }
            if (StringUtils.isNotEmpty(errMsg)) {
                return  errMsg;
            }
        } catch (Exception e) {
	        e.printStackTrace();
        } finally {
	        try {
	            bos.close();
	            ebos.close();
	            if (is != null) is.close();
	            if (es != null) es.close();
            } catch (IOException e) {
	            e.printStackTrace();
            }
        }
	    return "";
    }


    public static void main(String[] args) {
        String re = execShellCmd("/Users/zhongsj/Documents/ideaworkspace/radish/radish-agent/src/main/resources/monitor.sh", "CPJMN");
        AgentMonitorInfo agentMonitorInfo = new AgentMonitorInfo();
        agentMonitorInfo.setCpuCount(SystemUtil.cpuCount());
        if (StringUtils.isEmpty(agentMonitorInfo.getIp())) {
            agentMonitorInfo.setIp(IpUtil.getIp());
        }
        agentMonitorInfo.setOsName(SystemUtil.osName());
        agentMonitorInfo.setOsVersion(SystemUtil.osVersion());
        agentMonitorInfo.setAgentName(IpUtil.getHostName());

        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(agentMonitorInfo));
        List<Map<String, Object>> javaList = new ArrayList<>();
        Map<String, Map<String, Object>> netMap = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(re));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] kv = line.split(":");
                if (kv.length == 2 && StringUtils.isNotEmpty(kv[0]) && StringUtils.isNotEmpty(kv[1])) {
                    if (kv[0].startsWith("java")) { // java服务占用内存一对多关系，特殊处理
                        Map<String, Object> jMap = new HashMap<>();
                        jMap.put("name", kv[0].split("\\.")[1]);
                        jMap.put("rss", kv[1]);
                        javaList.add(jMap);
                    } else if (kv[0].startsWith("network")) { // 网卡一对多关系，特殊处理
                        String iface = kv[0].split("\\.")[1];
                        if (netMap.get(iface) == null) {
                            Map<String, Object> nMap = new HashMap<>();
                            nMap.put("iface", iface);
                            nMap.put(kv[0].split("\\.")[2], kv[1]);
                            netMap.put(iface, nMap);
                        } else {
                            netMap.get(iface).put(kv[0].split("\\.")[2], kv[1]);
                        }
                    } else {
                        map.put(kv[0], kv[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        map.put("javaMemoryList", javaList);
        map.put("networkIOList", netMap.values());
        agentMonitorInfo = JSON.parseObject(JSON.toJSONString(map), AgentMonitorInfo.class);
        System.out.println(agentMonitorInfo.getCpuIdle());
    }

}
