package priv.llf.job.support;

import org.springframework.context.ApplicationContext;

/**
 * @Author: Eleven
 * @Since: 2018/3/31 10:56
 * @Description:
 */
public class UserJobConstants {

    public static final String PROP_THREAD_COUNT = "org.quartz.threadPool.threadCount";

    public static final int DEFAULT_THREAD_COUNT = 10;

    public static final String applicationContextKey = ApplicationContext.class.getName();
}