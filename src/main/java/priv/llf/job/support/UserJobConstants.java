package priv.llf.job.support;

import org.springframework.context.ApplicationContext;
import priv.llf.job.serve.dto.args.UserJob;

/**
 * @Author: Eleven
 * @Since: 2018/3/31 10:56
 * @Description:
 */
public class UserJobConstants {

    public static final String PROP_THREAD_COUNT = "org.quartz.threadPool.threadCount";

    public static final int DEFAULT_THREAD_COUNT = 10;

    public static final String applicationContextKey = ApplicationContext.class.getName();

    public static final String schedulerNameKey="scheduler.name";

    public static final String schedulerClusterKey="scheduler.cluster";

    public static final String applicationName="priv-llf-user-job";

    public static final String userJobGroup="user-job";

    public static final String registryProtocol="zookeeper";

    public static final String userJobKey= UserJob.class.getName();





}
