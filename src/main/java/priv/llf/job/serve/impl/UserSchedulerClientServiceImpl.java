package priv.llf.job.serve.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import priv.llf.job.serve.UserSchedulerClientService;
import priv.llf.job.serve.dto.args.UserJob;
import priv.llf.job.serve.utils.QuartzUtils;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 21:02
 * @Description:
 */
@Slf4j
public class UserSchedulerClientServiceImpl implements UserSchedulerClientService {
    @Setter
    private Scheduler scheduler;

    public void addAndTriggerJob(UserJob userJob) throws SchedulerException {

        JobDetail jobDetail = QuartzUtils.buildJobDetail(userJob);
        scheduler.addJob(jobDetail,true);
        scheduler.triggerJob(jobDetail.getKey(),jobDetail.getJobDataMap());
        scheduler.start();
    }
}
