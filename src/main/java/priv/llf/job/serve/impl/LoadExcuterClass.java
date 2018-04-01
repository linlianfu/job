package priv.llf.job.serve.impl;

import org.springframework.context.ApplicationContext;
import priv.llf.job.client.support.UserJobExecutor;

/**
 * @Author: Eleven
 * @Since: 2018/4/1 11:24
 * @Description:
 */
public class LoadExcuterClass {

    static UserJobExecutor buildJobClass(String impl, ApplicationContext applicationContext){
        Class<? extends UserJobExecutor> jobClass = null;
        try {
            jobClass = (Class<? extends UserJobExecutor>) Class.forName(impl);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return applicationContext.getAutowireCapableBeanFactory().createBean(jobClass);
    }
}
