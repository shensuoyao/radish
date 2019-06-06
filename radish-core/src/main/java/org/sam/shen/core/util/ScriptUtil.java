package org.sam.shen.core.util;

import bsh.Interpreter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.model.Resp;

import java.io.*;

/**
  * 1、内嵌编译器如"PythonInterpreter"无法引用扩展包，因此推荐使用java调用控制台进程方式"Runtime.getRuntime().exec()"来运行脚本(shell或python)；
  * 2、因为通过java调用控制台进程方式实现，需要保证目标机器PATH路径正确配置对应编译器；
  * 3、暂时脚本执行日志只能在脚本执行结束后一次性获取，无法保证实时性；因此为确保日志实时性，可改为将脚本打印的日志存储在指定的日志文件上； 4、python
  * 异常输出优先级高于标准输出，体现在Log文件中，因此推荐通过logging方式打日志保持和异常信息一致；否则用prinf日志顺序会错乱
 * @author suoyao
 * @date 2018年8月1日 上午8:15:38
  * 
 */
@Slf4j
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
			log.error("error:", e);
			return -1;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					log.error("error:", e);
				}

			}
		}
	}

    public static String execShellWithResult(String command, String... params) {
        // 正常结果流
        ByteArrayOutputStream sucBos = new ByteArrayOutputStream();
        // 异常结果流
        ByteArrayOutputStream errBos = new ByteArrayOutputStream();
	    try {
            CommandLine commandLine = new CommandLine(command);
            if (params.length > 0) {
                commandLine.addArguments(params);
            }
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValues(null);
            // 设置超时时间
            ExecuteWatchdog watchdog = new ExecuteWatchdog(60 * 1000);
            executor.setWatchdog(watchdog);
            PumpStreamHandler pump = new PumpStreamHandler(sucBos, errBos);
            executor.setStreamHandler(pump);
            executor.execute(commandLine);

            String msg = sucBos.toString("utf-8").trim();
            String errMsg = errBos.toString("utf-8").trim();
            if (StringUtils.isNotEmpty(msg)) {
                return msg;
            }
            if (StringUtils.isNotEmpty(errMsg)) {
                return  errMsg;
            }
        } catch (IOException e) {
	        log.error("error:", e);
        } finally {
            try {
                sucBos.close();
                errBos.close();
            } catch (IOException e) {
                log.error("error:", e);
            }
        }
        return "";
    }


    /**
     * Create and authorize shell script
     * @author clock
     * @date 2018/12/7 下午1:39
     * @param filePath shell script file path
     * @param is shell script content
     */
    public static void createAndAuthShellScript(String filePath, InputStream is) {
        createAndAuthShellScript(new File(filePath), is);
    }

    /**
     * Create and authorize shell script
     * @author clock
     * @date 2018/12/7 下午1:39
     * @param file shell script file
     * @param is shell script content
     */
    public static void createAndAuthShellScript(File file, InputStream is) {
        try {
            // create shell script file
            if (file.exists() && !file.delete()) {
                throw new IOException("Can't delete original shell script file.");
            }
            if (!file.createNewFile()) {
                throw new IOException("Can't create shell script file.");
            }
            FileUtils.copyInputStreamToFile(is, file);

            // authorize script file
            try {
                ProcessBuilder pb = new ProcessBuilder("/bin/chmod", "755", file.getAbsolutePath());
                Process process = pb.start();
                process.waitFor();
            } catch (Exception e) {
                throw new IOException("Authorize script file failed.");
            }
        } catch (IOException e) {
            log.error("error:", e);
        }
    }

    /**
     * Execute java source file with bean shell
     * @author clock
     * @date 2019/4/28 下午5:41
     * @param filePath java source file path
     * @return result
     */
    public static Resp<String> execBshScriptWithResult(String filePath) {
        Resp<String> resp;
        Interpreter interpreter = new Interpreter();
        try {
            interpreter.source(filePath);
            resp = Resp.SUCCESS;
        } catch (Exception e) {
            resp = new Resp<>(Resp.FAIL.getCode(), Resp.FAIL.getMsg(), e.getMessage());
        }
        return resp;
    }

    public static void main(String[] args) {
        try {
            Object r = new Interpreter().source("/Users/zhongsj/Desktop/test.bsh");
            System.out.println(r.toString());
        } catch (Exception e) {
            log.error("error:", e);
        }
    }

}
