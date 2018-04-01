package priv.llf.job.serve.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import priv.llf.job.client.dto.args.UserJobDetailDto;
import priv.llf.job.serve.dto.args.UserJob;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 23:21
 * @Description:
 */
public class UserJobFactory {

    public static  UserJob buildUserJob(UserJobDetailDto userJodDetail, ApplicationContext applicationContext){
        Assert.notNull(userJodDetail,"任务信息不能为空");
        Assert.notNull(applicationContext,"上下文信息不能为空");
        UserJob  us = new UserJob();
        us.setGroup(userJodDetail.getGroup());
        us.setName(userJodDetail.getName());
        us.setJobClassName(userJodDetail.getJobClass().getName());
        us.setRemark(userJodDetail.getRemark());
        us.setDescription(userJodDetail.getDescription());
        us.setJobDataMap(userJodDetail.getJobDataMap());
        return us;
    }
}
