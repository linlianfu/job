package priv.llf.job.serve.utils;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import priv.llf.job.serve.impl.DefaultJobImpl;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 23:14
 * @Description:
 */
public class QuartzUtils {

    public static JobDetail buildJobDetail(){
        //待实现
        return JobBuilder.newJob(DefaultJobImpl.class).build();
    }
}
