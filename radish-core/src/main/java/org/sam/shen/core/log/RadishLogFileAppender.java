package org.sam.shen.core.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author suoyao
 * @date 2018年8月3日 下午5:56:29
  *   自定义日志适配器
 */
public class RadishLogFileAppender {
	private static Logger logger = LoggerFactory.getLogger(RadishLogFileAppender.class);

	private static String logBasePath = "/var/log/radish/";
	
	public static void initLogPath(String logPath) {
		if(StringUtils.isNotEmpty(logPath)) {
			logBasePath = logPath;
		}
		// Make LogPath
		File logBasePathDir = new File(logPath);
		try {
			FileUtils.forceMkdir(logBasePathDir);
		} catch (IOException e) {
			logger.error("Make Log Path Failed. [{}]", logPath, e);
		}
	}

	public static String getLogBasePath() {
		return logBasePath;
	}
	
	/**
	 * @author suoyao
	 * @date 下午3:07:41
	 * @param logId
	 * @return
	 *  创建日志文件
	 */
	public static String makeLogFile(String logId) {
		File logFilePath = new File(getLogBasePath(), new DateTime().toString("yyyyMMdd"));
		try {
			FileUtils.forceMkdir(logFilePath);
		} catch (IOException e) {
			logger.error("Make Log Path Failed. [{}]", logFilePath);
		}
		String logFileName = logFilePath.getPath().concat(File.separator).concat(logId).concat(".log");
		return logFileName;
	}
	
	/**
	 * @author suoyao
	 * @date 下午3:46:53
	 * @param logFileName
	 * @param appendLogLines
	 *   追加log日志
	 */
	public static void appendLog(String logFileName, List<String> appendLogLines) {
		if(StringUtils.isEmpty(logFileName)) {
			return;
		}
		File logFile = new File(logFileName);
		try {
			FileUtils.writeLines(logFile, appendLogLines, true);
		} catch (IOException e) {
			logger.error("Make Log File Failed. [{}]", logFile);
		}
	}
	
	/**
	 * @author suoyao
	 * @date 下午5:26:21
	 * @param logFileName
	 * @param beginLineNum
	 * @return
	 *   读取日志文件
	 */
	public static LogReader readLog(String logFileName, Integer beginLineNum) {
		if(null == beginLineNum) {
			beginLineNum = 0;
		}
		if(StringUtils.isEmpty(logFileName)) {
			return new LogReader(beginLineNum, 0, Arrays.asList("readLog fail, logFile not found"));
		}
		
		File logFile = new File(logFileName);
		if(!logFile.exists()) {
			return new LogReader(beginLineNum, 0, Arrays.asList("readLog fail, logFile not exists"));
		}
		
		// read file
		List<String> lines = Lists.newArrayList();
		int endLineNum = 0;
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new InputStreamReader(new FileInputStream(logFile), "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				endLineNum = reader.getLineNumber(); // [from, to], start as 1
				if (endLineNum >= beginLineNum) {
					lines.add(line);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		// result
		LogReader logReader = new LogReader(beginLineNum, (endLineNum + 1), lines);
		return logReader;
	}
	
	/**
	 * @author suoyao
	 * @date 下午5:56:08
	 * @param logFileName
	 * @return
	 *   将日志全部读取
	 */
	public static List<String> readLines(String logFileName){
		List<String> lines = Lists.newArrayList();
		if(StringUtils.isEmpty(logFileName)) {
			return Arrays.asList("readLog fail, logFile not found");
		}
		File logFile = new File(logFileName);
		if(!logFile.exists()) {
			return Arrays.asList("readLog fail, logFile not exists");
		}
		
		try {
			return FileUtils.readLines(logFile, "UTF-8");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return lines;
		}
	}
	
}
