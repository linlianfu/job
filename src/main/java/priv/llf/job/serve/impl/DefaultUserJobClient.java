package priv.llf.job.serve.impl;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import priv.llf.job.client.dto.args.UserJobDetailDto;
import priv.llf.job.client.service.UserJobClient;
import priv.llf.job.serve.UserSchedulerClientService;
import priv.llf.job.serve.dto.args.UserJob;
import priv.llf.job.serve.utils.UserJobFactory;
import priv.llf.job.support.UserJobConstants;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 23:06
 * @Description:  实现client的UserJobClient，对消费端隐藏
 */
@Slf4j
public class DefaultUserJobClient implements UserJobClient{

    protected Scheduler scheduler;

    protected UserSchedulerClientService userSchedulerClientService;

    protected ApplicationContext applicationContext;

    public DefaultUserJobClient(Scheduler scheduler,UserSchedulerClientService userSchedulerClientService) throws SchedulerException {
        Assert.notNull(userSchedulerClientService,"用户任务所使用的UserSchedulerServerService不能为空");
        Assert.notNull(scheduler,"用户任务所使用的调度器不能为空");
        this.userSchedulerClientService = userSchedulerClientService;
        this.scheduler = scheduler;
        this.applicationContext = (ApplicationContext)scheduler.getContext().get(UserJobConstants.applicationContextKey);
    }


    public void execute(UserJobDetailDto userJobDetailDto) throws SchedulerException {
        UserJob userJob = UserJobFactory.buildUserJob(userJobDetailDto,applicationContext);
        userSchedulerClientService.addAndTriggerJob(userJob);
    }
}
