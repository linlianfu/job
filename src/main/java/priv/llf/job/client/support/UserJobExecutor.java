package priv.llf.job.client.support;

import org.quartz.JobDataMap;
import priv.llf.job.client.dto.response.UserJobExecuteResult;
import priv.llf.job.serve.dto.args.UserJob;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 22:15
 * @Description:
 */
public interface UserJobExecutor {

    UserJobExecuteResult execute(UserJob userJob, JobDataMap jobDataMap);
}
