package priv.llf.job.serve.impl;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import priv.llf.job.client.support.UserJobExecutor;
import priv.llf.job.serve.dto.args.UserJob;
import priv.llf.job.support.UserJobConstants;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 22:17
 * @Description:
 */
public class DefaultJobImpl implements Job {

    Log logger =  LogFactory.getLog(DefaultUserJobClient.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        /**
         * 1.此处用过context获取实现类的名字，通过类反射机制得到实际执行的类
         */
        try {
            ApplicationContext applicationContext = (ApplicationContext)context.getScheduler().getContext().get(UserJobConstants.applicationContextKey);
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            UserJob userJob = (UserJob)jobDataMap.get(UserJobConstants.userJobKey);
            UserJobExecutor executor = LoadExcuterClass.buildJobClass(userJob.getJobClassName(),applicationContext);
            executor.execute(userJob,jobDataMap);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }
}
