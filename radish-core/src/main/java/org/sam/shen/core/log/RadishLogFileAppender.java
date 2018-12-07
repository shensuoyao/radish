package org.sam.shen.core.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.sam.shen.core.agent.RadishAgent;

import com.google.common.collect.Lists;

/**
 * 自定义日志适配器
 * @author suoyao
 * @date 2018年8月3日 下午5:56:29
 */
@Slf4j
public class RadishLogFileAppender {

	public static void initLogPath(String logPath) {
		// Make LogPath
		File logBasePathDir = new File(logPath);
		try {
			FileUtils.forceMkdir(logBasePathDir);
		} catch (IOException e) {
			log.error("Make Log Path Failed. [{}]", logPath, e);
		}
	}

	public static void initShPath(String shPath) {
        // Make shPath
        File shBasePathDir = new File(shPath);
        try {
            FileUtils.forceMkdir(shBasePathDir);
        } catch (IOException e) {
            log.error("Make Shell Path Failed. [{}]", shPath, e);
        }
    }

	/**
	 * @author suoyao
	 * @date 下午3:07:41
	 * @param logId
	 * @return
	 *  创建日志文件
	 */
	public static String makeLogFile(String logId) {
		File logFilePath = new File(RadishAgent.getLogPath(), new DateTime().toString("yyyyMMdd"));
		try {
			FileUtils.forceMkdir(logFilePath);
		} catch (IOException e) {
			log.error("Make Log Path Failed. [{}]", logFilePath);
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
			log.error("Make Log File Failed. [{}]", logFile);
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
			log.error(e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
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
			log.error(e.getMessage(), e);
			return lines;
		}
	}
	
}
