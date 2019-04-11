package org.sam.shen.scheduing.cluster;

import org.sam.shen.scheduing.vo.JobSchedulerVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author clock
 * @date 2019/4/10 下午4:55
 */
public class LoadPacket {

    private Map<Integer, List<JobSchedulerVo>> toLoadJobMap;

    private Map<Integer, List<JobSchedulerVo>> loadingJobMap;

    private List<JobSchedulerVo> errorJobs;

    public LoadPacket() {
        this.toLoadJobMap = new HashMap<>();
        this.loadingJobMap = new HashMap<>();
        this.errorJobs = new ArrayList<>();
    }

    public void addToLoadJobs(Integer nid, List<JobSchedulerVo> jobs) {
        toLoadJobMap.put(nid, jobs);
    }

    public List<JobSchedulerVo> getToLoadJobs(Integer nid) {
        return toLoadJobMap.get(nid);
    }

    public void loadJob(Integer nid) {
        loadingJobMap.put(nid, toLoadJobMap.get(nid));
        toLoadJobMap.remove(nid);
    }

    public void addErrorJob(JobSchedulerVo jobs) {
        errorJobs.add(jobs);
    }

    public void addErrorJobs(List<JobSchedulerVo> jobs) {
        errorJobs.addAll(jobs);
    }

    public void removeLoadedJobs(Integer nid) {
        loadingJobMap.remove(nid);
    }

    public void setToLoadJobMap(Map<Integer, List<JobSchedulerVo>> toLoadJobMap) {
        this.toLoadJobMap = toLoadJobMap;
    }

    public boolean isEmpty() {
        return toLoadJobMap.size() == 0 && loadingJobMap.size() == 0;
    }

    public Map<Integer, List<JobSchedulerVo>> getToLoadJobMap() {
        return toLoadJobMap;
    }

    public Map<Integer, List<JobSchedulerVo>> getLoadingJobMap() {
        return loadingJobMap;
    }

    public List<JobSchedulerVo> getErrorJobs() {
        return errorJobs;
    }

    public void clearLoadMap() {
        toLoadJobMap.clear();
        loadingJobMap.clear();
    }

    public void clearAll() {
        toLoadJobMap.clear();
        loadingJobMap.clear();
        errorJobs.clear();
    }
}
