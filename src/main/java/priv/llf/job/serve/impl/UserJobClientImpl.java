package priv.llf.job.serve.impl;

import org.quartz.SchedulerException;
import priv.llf.job.client.dto.UserJobDetailDto;
import priv.llf.job.client.service.UserJobClient;
import priv.llf.job.serve.UserSchedulerClientService;
import priv.llf.job.serve.dto.UserJob;
import priv.llf.job.serve.utils.UserJobFactory;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 23:06
 * @Description:  实现client的UserJobClient，对消费端隐藏
 */
public class UserJobClientImpl implements UserJobClient {

    UserSchedulerClientService userSchedulerClientService;


    public void execute(UserJobDetailDto userJobDetailDto) throws SchedulerException {
        UserJob userJob = UserJobFactory.buildUserJob(userJobDetailDto);
        userSchedulerClientService.addAndTriggerJob(userJob);
    }
}
