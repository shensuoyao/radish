package org.sam.shen.scheduing.controller.portal;

import java.util.Arrays;

import org.sam.shen.core.constants.HandlerTypeEnum;
import org.springframework.stereotype.Controller;
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
		model.addObject("jobType", Arrays.asList(HandlerTypeEnum.values()));
		model.setViewName("frame/job/job_add");
		return model;
	}
	
	@RequestMapping(value = "job-save", method = RequestMethod.POST)
	public ModelAndView jobSave(ModelAndView model, @RequestParam("cmd") String cmd) {
		model.setViewName("frame/job/job_add");
		return model;
	}
	
}
