package org.sam.shen.scheduing.controller.portal;

import java.util.Arrays;
import java.util.List;

import org.sam.shen.core.constants.HandlerFailStrategy;
import org.sam.shen.core.constants.HandlerType;
import org.sam.shen.scheduing.entity.JobInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 *  任务
 * @author suoyao
 * @date 2018年8月15日 上午11:31:59
  * 
 */
@Controller
@RequestMapping(value="job")
public class JobController {

	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public ModelAndView toJobPage(ModelAndView model) {
		model.setViewName("frame/job/job");
		return model;
	}
	
	@RequestMapping(value = "job-add", method = RequestMethod.GET)
	public ModelAndView jobAdd(ModelAndView model) {
		model.addObject("jobType", Arrays.asList(HandlerType.values()));
		model.addObject("handlerFailStrategy", Arrays.asList(HandlerFailStrategy.values()));
		model.setViewName("frame/job/job_add");
		return model;
	}

	@RequestMapping(value = "job-save", method = RequestMethod.POST)
	public ModelAndView jobSave(ModelAndView model, @ModelAttribute JobInfo jobInfo,
	        @RequestParam("parameter") String parameter, @RequestParam("parentJob") List<String> parentJob,
	        @RequestParam("chidJob") List<String> chidJob, @RequestParam("agentHandlers") List<String> agentHandlers) {
		model.setViewName("frame/job/job_add");
		String[] params = parameter.split(System.lineSeparator());
		jobInfo.setParams(Arrays.asList(params));
//		try {
//			JobInfo jobInfo = new JobInfo();
//			jobInfo.setId(1L);
//			jobInfo.setJobName("测试");
//			jobInfo.setCrontab("*/5 * * * * ?");
//			RadishDynamicScheduler.addJob(jobInfo);
//		} catch (SchedulerException e) {
//			e.printStackTrace();
//		}
		return model;
	}
	
}
