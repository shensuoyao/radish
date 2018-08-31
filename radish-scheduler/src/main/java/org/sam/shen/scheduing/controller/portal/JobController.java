package org.sam.shen.scheduing.controller.portal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.constants.HandlerFailStrategy;
import org.sam.shen.core.constants.HandlerType;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.entity.RespPager;
import org.sam.shen.scheduing.service.AgentService;
import org.sam.shen.scheduing.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.Page;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 任务
 * 
 * @author suoyao
 * @date 2018年8月15日 上午11:31:59
 * 
 */
@Controller
@RequestMapping(value = "portal")
public class JobController {

	@Autowired
	private JobService jobService;

	@Autowired
	private AgentService agentServie;

	@RequestMapping(value = { "job", "job/" }, method = RequestMethod.GET)
	public ModelAndView toJobPage(ModelAndView model) {
		model.setViewName("frame/job/job");
		return model;
	}

	/**
	 * 到任务新增页面
	 * 
	 * @author suoyao
	 * @date 下午3:35:53
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "job-add", method = RequestMethod.GET)
	public ModelAndView jobAdd(ModelAndView model) {
		model.addObject("handlerType", Arrays.asList(HandlerType.values()));
		model.addObject("handlerFailStrategy", Arrays.asList(HandlerFailStrategy.values()));
		model.setViewName("frame/job/job_add");
		return model;
	}

	/**
	 * 新增 Job
	 * 
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
		if (null != agentHandlers && agentHandlers.size() > 0) {
			jobInfo.setExecutorHandlers(Joiner.on(",").join(agentHandlers));
		}
		jobInfo.setUpdateTime(new Date());
		if(null == jobInfo.getId()) {
			 // 新增Job
			jobInfo.setCreateTime(new Date());
			jobService.addJobinfo(jobInfo);
		} else {
			 // 更新
			jobService.upgradeJobInfo(jobInfo);
		}
		return model;
	}

	/**
	 * 分页查询JobInfo
	 * 
	 * @author suoyao
	 * @date 下午6:19:08
	 * @param page
	 * @param limit
	 * @param jobName
	 * @return
	 */
	@RequestMapping(value = "job/json-pager", method = RequestMethod.GET)
	@ResponseBody
	public RespPager<Page<JobInfo>> queryJobInfoForJsonPager(@RequestParam("page") Integer page,
	        @RequestParam("limit") Integer limit,
	        @RequestParam(value = "jobName", required = false, defaultValue = "") String jobName) {
		if (null == page) {
			page = 1;
		}
		if (null == limit) {
			limit = 10;
		}
		Page<JobInfo> pager = jobService.queryJobInfoForPager(page, limit, jobName);
		return new RespPager<>(pager.getPageSize(), pager.getTotal(), pager);
	}

	@RequestMapping(value = "job/json", method = RequestMethod.GET)
	@ResponseBody
	public Resp<List<JobInfo>> queryJobInfoForJsonPager(
	        @RequestParam(value = "jobName", required = false, defaultValue = "") String jobName) {
		List<JobInfo> list = jobService.queryJobInfoForList(jobName);
		if (null == list) {
			list = Collections.emptyList();
		}
		return new Resp<>(list);
	}

	/**
	 * 任务视图
	 * 
	 * @author suoyao
	 * @date 下午4:36:54
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "job-view/{id}", method = RequestMethod.GET)
	public ModelAndView view(ModelAndView model, @PathVariable("id") Long id) {
		// 获取任务对象
		JobInfo jobInfo = jobService.findJobInfo(id);
		model.addObject("jobInfo", jobInfo);
		// 重装Agent-Handler显示格式
		if (StringUtils.isNotEmpty(jobInfo.getExecutorHandlers())) {
			Set<Long> ids = Sets.newHashSet();
			List<String> sp = Splitter.onPattern(",|-").splitToList(jobInfo.getExecutorHandlers());
			Stream.iterate(0, i -> i + 1).limit(sp.size()).forEach(i -> {
				if (i % 2 == 0) {
					ids.add(Long.valueOf(sp.get(i)));
				}
			});
			List<Agent> agents = agentServie.queryAgentInIds(Lists.newArrayList(ids));
			if (agents.size() > 0) {
				Map<String, String> agentMap = Maps.newHashMap();
				agents.forEach(agent -> agentMap.put(String.valueOf(agent.getId()), agent.getAgentName()));
				List<String> handlers = Lists.newArrayList();
				Stream.iterate(0, i -> i + 2).limit(sp.size() / 2)
				        .forEach(i -> handlers.add(agentMap.get(sp.get(i)) + Constant.SPLIT_CHARACTER + sp.get(i + 1)));
				model.addObject("handlers", handlers);
			}
		}
		// 定义任务流程图视图
		Map<String, Set<String>> dagre = jobService.dagre(jobInfo);
		model.addObject("states", dagre.get("nodes"));
		model.addObject("edges", dagre.get("edges"));
		model.setViewName("frame/job/job_view");
		return model;
	}

	/**
	 * @author suoyao
	 * @date 下午12:20:10
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value = { "job-edit/", "job-edit/{id}" }, method = RequestMethod.GET)
	public ModelAndView jobEdit(ModelAndView model, @PathVariable(value = "id", required = false) Long id) {
		if(null != id) {
			JobInfo jobInfo = jobService.findJobInfo(id);
			model.addObject("jobInfo", jobInfo);
			
			List<Long> ids = Lists.newArrayList();
			if(StringUtils.isNotEmpty(jobInfo.getParentJobId())) {
				Arrays.asList(jobInfo.getParentJobId().split(",")).forEach(jid -> ids.add(Long.valueOf(jid)));
			}
			if(StringUtils.isNotEmpty(jobInfo.getChildJobId())) {
				Arrays.asList(jobInfo.getChildJobId().split(",")).forEach(jid -> ids.add(Long.valueOf(jid)));
			}
			if(ids.size() > 0) {
				List<JobInfo> parentJob = Lists.newArrayList();
				List<JobInfo> childJob = Lists.newArrayList();
				List<JobInfo> depend = jobService.queryJobInfoByIds(ids);
				depend.forEach(job -> {
					
					if(null != jobInfo.getParentJobId() && jobInfo.getParentJobId().indexOf(String.valueOf(job.getId())) >= 0) {
						parentJob.add(job);
					}
					if(null != jobInfo.getChildJobId() && jobInfo.getChildJobId().indexOf(String.valueOf(job.getId())) >= 0) {
						childJob.add(job);
					}
				});
				model.addObject("parentJob", parentJob);
				model.addObject("childJob", childJob);
			}
			if(StringUtils.isNotEmpty(jobInfo.getExecutorHandlers())) {
				model.addObject("handlers", Arrays.asList(jobInfo.getExecutorHandlers().split(",")));
			}
			
		}
		model.addObject("handlerType", Arrays.asList(HandlerType.values()));
		model.addObject("handlerFailStrategy", Arrays.asList(HandlerFailStrategy.values()));
		model.setViewName("frame/job/job_edit");
		return model;
	}

}