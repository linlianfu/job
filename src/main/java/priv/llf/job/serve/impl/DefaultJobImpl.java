package priv.llf.job.serve.impl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 22:17
 * @Description:
 */
public class DefaultJobImpl implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
        /**
         * 1.此处用过context获取实现类的名字，通过类反射机制得到实际执行的类
         */
    }
}
