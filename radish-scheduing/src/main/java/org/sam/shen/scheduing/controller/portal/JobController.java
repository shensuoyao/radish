package org.sam.shen.scheduing.controller.portal;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.sam.shen.core.constants.HandlerFailStrategy;
import org.sam.shen.core.constants.HandlerType;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.entity.RespPager;
import org.sam.shen.scheduing.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.Page;
import com.google.common.base.Joiner;

/**
 * 任务
 * 
 * @author suoyao
 * @date 2018年8月15日 上午11:31:59
 * 
 */
@Controller
@RequestMapping(value = "job")
public class JobController {
	
	@Autowired
	private JobService jobService;

	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public ModelAndView toJobPage(ModelAndView model) {
		model.setViewName("frame/job/job");
		return model;
	}

	/**
	 *  到任务新增页面
	 * @author suoyao
	 * @date 下午3:35:53
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "job-add", method = RequestMethod.GET)
	public ModelAndView jobAdd(ModelAndView model) {
		model.addObject("jobType", Arrays.asList(HandlerType.values()));
		model.addObject("handlerFailStrategy", Arrays.asList(HandlerFailStrategy.values()));
		model.setViewName("frame/job/job_add");
		return model;
	}

	/**
	 *  新增 Job
	 * @author suoyao
	 * @date 下午3:36:08
	 * @param model
	 * @param jobInfo
	 * @param parameter
	 * @param parentJob
	 * @param chidJob
	 * @param agentHandlers
	 * @return
	 */
	@RequestMapping(value = "job-save", method = RequestMethod.POST)
	public ModelAndView jobSave(ModelAndView model, @ModelAttribute JobInfo jobInfo,
	        @RequestParam("parentJob") List<String> parentJob, @RequestParam("chidJob") List<String> chidJob,
	        @RequestParam("agentHandlers") List<String> agentHandlers) {
		model.setViewName("frame/job/job_add");
		if (null != parentJob && parentJob.size() > 0) {
			jobInfo.setParentJobId(Joiner.on(",").join(parentJob));
		}
		if (null != chidJob && chidJob.size() > 0) {
			jobInfo.setChildJobId(Joiner.on(",").join(chidJob));
		}
		if(null != agentHandlers && agentHandlers.size() > 0) {
			jobInfo.setExecutorHandlers(Joiner.on(",").join(agentHandlers));
		}
		jobInfo.setCreateTime(new Date());
		jobInfo.setUpdateTime(new Date());
		/*if(null != agentHandlers && agentHandlers.size() > 0) {
			Map<Long, Set<String>> executorHandlers = Maps.newHashMap();
			for(String ah : agentHandlers) {
				String[] arr = ah.split(Constant.SPLIT_CHARACTER);
				if(executorHandlers.containsKey(Long.valueOf(arr[0]))) {
					executorHandlers.get(Long.valueOf(arr[0])).add(arr[1]);
				} else {
					executorHandlers.put(Long.valueOf(arr[0]), new HashSet<String>() {
						private static final long serialVersionUID = -8759217899959786853L;
						{
							add(arr[1]);
						}
					});
				}
			}
		}*/
		jobService.addJobinfo(jobInfo);
		return model;
	}
	
	/**
	 *  分页查询JobInfo
	 * @author suoyao
	 * @date 下午6:19:08
	 * @param page
	 * @param limit
	 * @param jobName
	 * @return
	 */
	@RequestMapping(value = "json-pager", method = RequestMethod.GET)
	@ResponseBody
	public RespPager<Page<JobInfo>> queryJobInfoForJsonPager(@RequestParam("page") Integer page,
	        @RequestParam("limit") Integer limit,
	        @RequestParam(value = "jobName", required = false, defaultValue = "") String jobName) {
		if(null == page) {
			page = 1;
		}
		if(null == limit) {
			limit = 10;
		}
		Page<JobInfo> pager = jobService.queryJobInfoForPager(page, limit, jobName);
		return new RespPager<>(pager.getPageSize(), pager.getTotal(), pager);
	}

}
