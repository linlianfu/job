package priv.llf.job.serve.utils;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.util.Assert;
import priv.llf.job.serve.dto.args.UserJob;
import priv.llf.job.serve.impl.DefaultJobImpl;
import priv.llf.job.support.UserJobConstants;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 23:14
 * @Description:
 */
public class QuartzUtils {

    public static JobDetail buildJobDetail(UserJob userJob){
        Assert.notNull(userJob,"任务信息不能为空");
        String name=userJob.getName();
        String group=userJob.getGroup();
        JobDataMap map=userJob.getJobDataMap();
        map.put(UserJobConstants.userJobKey, userJob);
        return JobBuilder.newJob(DefaultJobImpl.class)
                         .withIdentity(name,group)
                         .usingJobData(map)
                         .requestRecovery(userJob.isRequestsRecovery())
                         .storeDurably(userJob.isDurable())
                         .build();
    }
}
