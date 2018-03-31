package priv.llf.job.serve.utils;

import org.springframework.context.ApplicationContext;
import priv.llf.job.client.dto.args.UserJobDetailDto;
import priv.llf.job.serve.dto.args.UserJob;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 23:21
 * @Description:
 */
public class UserJobFactory {

    public static  UserJob buildUserJob(UserJobDetailDto userJodDetail, ApplicationContext applicationContext){
        UserJob  us = new UserJob();
//        Assert.notNull(userJob,"任务信息不能为空");
//        String name=userJob.getName();
//        String group=userJob.getGroup();
//        JobDataMap map=userJob.getJobDataMap();
//        JobDataMap jobDataMap=new JobDataMap();
//        if(!map.isEmpty()) {
//
//        }
        return us;
    }
}
