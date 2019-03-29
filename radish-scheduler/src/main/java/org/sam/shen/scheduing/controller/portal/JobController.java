package org.sam.shen.scheduing.controller.portal;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.constants.*;
import org.sam.shen.core.log.LogReader;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.constants.SchedConstant;
import org.sam.shen.scheduing.entity.*;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.sam.shen.scheduing.service.AgentService;
import org.sam.shen.scheduing.service.JobEventService;
import org.sam.shen.scheduing.service.JobService;
import org.sam.shen.scheduing.vo.JobEventTreeNode;
import org.sam.shen.scheduing.vo.JobSchedulerVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.Page;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpSession;

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
	
	private Logger logger = LoggerFactory.getLogger(JobController.class);

	@Autowired
	private JobService jobService;
	
	@Autowired
	private JobEventService jobEventService;

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
		model.addObject("distributionType", Arrays.asList(DistributionType.values()));
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
	 * @param parentJob
	 * @param agentHandlers
	 * @return
	 */
	@RequestMapping(value = "job-save", method = RequestMethod.POST)
	public ModelAndView jobSave(ModelAndView model, @ModelAttribute JobInfo jobInfo,
								@RequestParam("parentJob") List<String> parentJob,
								@RequestParam("agentHandlers") List<String> agentHandlers,
								HttpSession session) {
        model.setViewName("frame/job/job_add");
		if (null != parentJob && parentJob.size() > 0) {
			jobInfo.setParentJobId(Joiner.on(",").join(parentJob));
		}
		if (null != agentHandlers && agentHandlers.size() > 0) {
			jobInfo.setExecutorHandlers(Joiner.on(",").join(agentHandlers));
		}
		jobInfo.setUpdateTime(new Date());
		if(null == jobInfo.getId()) {
			 // 新增Job
			jobInfo.setCreateTime(new Date());
			User user = (User) session.getAttribute("user");
			if (SchedConstant.ADMINISTRATOR.equals(user.getUname())){ // 如果管理员登陆查询所有数据
			    user.setId(null);
            }
			jobInfo.setUserId(user.getId());
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
                                                             @RequestParam(value = "jobName", required = false, defaultValue = "") String jobName,
                                                             HttpSession session) {
		if (null == page) {
			page = 1;
		}
		if (null == limit) {
			limit = 10;
		}
		User user = (User) session.getAttribute("user");
        if (SchedConstant.ADMINISTRATOR.equals(user.getUname())){ // 如果管理员登陆查询所有数据
            user.setId(null);
        }
		Page<JobInfo> pager = jobService.queryJobInfoForPager(page, limit, jobName, user.getId());
		return new RespPager<>(pager.getPageSize(), pager.getTotal(), pager);
	}

	@RequestMapping(value = "job/json", method = RequestMethod.GET)
	@ResponseBody
	public Resp<List<JobInfo>> queryJobInfoForJsonPager(
	        @RequestParam(value = "jobName", required = false, defaultValue = "") String jobName, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (SchedConstant.ADMINISTRATOR.equals(user.getUname())){ // 如果管理员登陆查询所有数据
            user.setId(null);
        }
		List<JobInfo> list = jobService.queryJobInfoForList(jobName, user.getId());
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
			if(ids.size() > 0) {
				List<JobInfo> parentJob = Lists.newArrayList();
				List<JobInfo> depend = jobService.queryJobInfoByIds(ids);
				depend.forEach(job -> {
					
					if(null != jobInfo.getParentJobId() && jobInfo.getParentJobId().indexOf(String.valueOf(job.getId())) >= 0) {
						parentJob.add(job);
					}
				});
				model.addObject("parentJob", parentJob);
			}
			if(StringUtils.isNotEmpty(jobInfo.getExecutorHandlers())) {
				model.addObject("handlers", Arrays.asList(jobInfo.getExecutorHandlers().split(",")));
			}
			
		}
		model.addObject("handlerType", Arrays.asList(HandlerType.values()));
		model.addObject("handlerFailStrategy", Arrays.asList(HandlerFailStrategy.values()));
        model.addObject("distributionType", Arrays.asList(DistributionType.values()));
		model.setViewName("frame/job/job_edit");
		return model;
	}
	
	/**
	 *  查询正在调度中的job
	 * @author suoyao
	 * @date 上午11:31:31
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "job-scheduler", method = RequestMethod.GET)
	public ModelAndView jobInScheduler(ModelAndView model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (SchedConstant.ADMINISTRATOR.equals(user.getUname())){ // 如果管理员登陆查询所有数据
			user.setId(null);
		}
		List<JobSchedulerVo> jobs = RadishDynamicScheduler.listJobsInScheduler(user.getId());
		model.addObject("jobs", jobs);
		model.setViewName("frame/job/job_scheduler");
		return model;
	}
	
	@RequestMapping(value = "job-event", method = RequestMethod.GET)
	public ModelAndView jobEvent(ModelAndView model) {
		
		model.setViewName("frame/job/job_event");
		return model;
	}

	/**
	 *  查询事件分页
	 * @author suoyao
	 * @date 上午11:31:11
	 * @param page
	 * @param limit
	 * @param stat
	 * @return
	 */
	@RequestMapping(value = "job-event/json-pager", method = RequestMethod.GET)
	@ResponseBody
	public RespPager<Page<JobEvent>> queryJobEventForJsonPager(@RequestParam("page") Integer page
            , @RequestParam("limit") Integer limit
            , @RequestParam(value = "stat", required = false) EventStatus stat
            , HttpSession session) {
		if (null == page) {
			page = 1;
		}
		if (null == limit) {
			limit = 10;
		}
		if(null == stat) {
			stat = EventStatus.READY;
		}
		User user = (User) session.getAttribute("user");
        if (SchedConstant.ADMINISTRATOR.equals(user.getUname())){ // 如果管理员登陆查询所有数据
            user.setId(null);
        }
		Page<JobEvent> pager = jobEventService.queryJobEventForPager(page, limit, stat, user.getId());
		return new RespPager<>(pager.getPageSize(), pager.getTotal(), pager);
	}
	
	@RequestMapping(value = "job-event-log", method = RequestMethod.GET)
	public ModelAndView eventLog(ModelAndView model, @RequestParam(value = "eventId", required = false) String eventId,
	        @RequestParam(value = "agentId", required = false) Long agentId) {
		model.setViewName("frame/job/job_event_log");
		if(StringUtils.isEmpty(eventId) || null == agentId) {
			model.addObject("logs", Lists.newArrayList("Event ID 为空或 Agent ID 为空."));
			return model;
		}
		LogReader logReader = jobEventService.readEventLogFromAgent(eventId, agentId);
		if(null == logReader) {
			model.addObject("logs", Lists.newArrayList("日志为空"));
		}
		model.addObject("logs", logReader.getLogLines());
		return model;
	}

	@ResponseBody
    @RequestMapping(value = "job-event-save", method = RequestMethod.POST)
	public Resp<String> saveJobEvent(Long jobId) {
	    if (jobId == null) {
	        return new Resp<>(0, "job id cannot be null.");
        }
        JobInfo jobInfo = jobService.findJobInfo(jobId);
	    if (jobInfo == null) {
            return new Resp<>(0, "invalid job id.");
        }
        if (jobInfo.getEnable() == Constant.NO || StringUtils.isNotEmpty(jobInfo.getCrontab()) || StringUtils.isNotEmpty(jobInfo.getParentJobId())) {
            return new Resp<>(0, "invalid job.");
        }
        List<JobEvent> jobEvents = jobEventService.queryJobEventByJobId(jobId);
	    if (jobEvents != null && jobEvents.size() > 0) {
            return new Resp<>(0, "exist job event.");
        }
        if (RadishDynamicScheduler.addJobEvent(jobId)) {
	        return new Resp<>(1, "add job event success.");
        } else {
            return new Resp<>(0, "add job event failed.");
        }
    }

    @RequestMapping(value = "job-event-view/{eventId}", method = RequestMethod.GET)
    public ModelAndView jobEventView(ModelAndView modelAndView, @PathVariable String eventId) {
	    modelAndView.addObject("eventId", eventId);
	    modelAndView.setViewName("frame/job/job_event_view");
	    return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "job-event-tree/{eventId}", method = RequestMethod.GET)
    public JobEventTreeNode queryJobEventTree(@PathVariable String eventId) {
        JobEvent root = jobEventService.queryRootJobEvent(eventId);
        List<JobEventTreeNode> treeNodes = jobEventService.queryChildEvents(root);
        return buildTree(treeNodes);
    }


    private JobEventTreeNode buildTree(List<JobEventTreeNode> treeNodes) {
	    JobEventTreeNode root = null;
	    for (JobEventTreeNode treeNode : treeNodes) {
	        if (StringUtils.isEmpty(treeNode.getPid())){
	            root = treeNode;
            }
            for (JobEventTreeNode childNode : treeNodes) {
	            if (treeNode.getId().equals(childNode.getPid())) {
	                if (treeNode.getChildren() == null) {
	                    treeNode.setChildren(new ArrayList<>());
                    }
	                treeNode.getChildren().add(childNode);
                }
            }
        }
        return root;
    }

    @ResponseBody
    @RequestMapping(value = "retry-handle-event", method = RequestMethod.POST)
    public Resp<String> retryHandleEvent(@RequestBody JobEvent jobEvent) {
	    if (jobEvent == null || StringUtils.isEmpty(jobEvent.getEventId())) {
	        return new Resp<>(Resp.FAIL.getCode(), "事件ID不能为空！");
        }
	    try {
            jobEventService.rehandleFailedEvent(jobEvent);
        } catch (Exception e) {
            return new Resp<>(Resp.FAIL.getCode(), "重新添加事件失败！");
        }
        return Resp.SUCCESS;
    }

    /**
     * 修改事件的优先级
     * @author clock
     * @date 2018/12/18 下午1:21
     * @param jobEvent 事件
     * @return 修改结果
     */
	@ResponseBody
	@RequestMapping(value = "update-event-priority", method = RequestMethod.POST)
	public Resp<String> updateEventPriority(@RequestBody JobEvent jobEvent) {
		if (jobEvent == null || StringUtils.isEmpty(jobEvent.getEventId())) {
			return new Resp<>(Resp.FAIL.getCode(), "事件ID不能为空！");
		}
		try {
			jobEventService.updateEventPriority(jobEvent);
		} catch (Exception e) {
			return new Resp<>(Resp.FAIL.getCode(), "修改事件优先级失败！");
		}
		return Resp.SUCCESS;
	}

    /**
     * 上传参数附件
     * @author clock
     * @date 2019/1/2 上午11:22
     * @param multipartFile 参数附件
     * @return 上传参数结果
     */
	@ResponseBody
    @RequestMapping(value = "upload-param-file", method = RequestMethod.POST)
    public Resp<String> uploadParamFile(@RequestParam("file") MultipartFile multipartFile) {
	    if (multipartFile == null || multipartFile.getOriginalFilename() == null) {
	        return new Resp<>(Resp.FAIL.getCode(), "上传文件不能为空!");
        }
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
	    String prefix = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().indexOf(".") + 1);
	    String fileName = uuid.concat(".").concat(prefix);
	    File file = new File(SchedConstant.PARAMS_FILE_PATH.concat(File.separator).concat(fileName));
	    try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        } catch (IOException e) {
            return new Resp<>(Resp.FAIL.getCode(), "上传文件失败!");
        }
        return new Resp<>(Resp.SUCCESS.getCode(), fileName);
    }

    /**
     * 下载参数附件
     * @author clock
     * @date 2019/1/2 下午1:17
     * @param fileName 参数附件名称
     * @return 文件输出流
     */
    @GetMapping(value = "download-param-file")
    public StreamingResponseBody downloadParamFile(@RequestParam String fileName) {
        File file = new File(SchedConstant.PARAMS_FILE_PATH.concat(File.separator).concat(fileName));
	    return outputStream -> {
            FileInputStream fis = new FileInputStream(file);
            IOUtils.copy(fis, outputStream);
        };
    }
	
}
