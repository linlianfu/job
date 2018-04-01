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
        //此处重新new一个jobDataMap是为了防止
        //如果在userJob的jobDataMap在put自己的话，
        //会在toString()的时候会出现无限循环，导致堆栈溢出
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(UserJobConstants.userJobKey, userJob);
        jobDataMap.putAll(map);
        return JobBuilder.newJob(DefaultJobImpl.class)
                         .withIdentity(name,group)
                         .usingJobData(jobDataMap)
                         .requestRecovery(userJob.isRequestsRecovery())
                         .storeDurably(userJob.isDurable())
                         .build();
    }
}
