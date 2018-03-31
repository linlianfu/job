package priv.llf.job.serve;

import org.quartz.SchedulerException;
import priv.llf.job.serve.dto.args.UserJob;

/**
 * @Author: Eleven
 * @Since: 2018/3/10 11:04
 * @Description:
 */
public interface UserSchedulerClientService {

    /**
     * 添加并立即执行一个异步任务
     * @param userJob
     */
    public void addAndTriggerJob(UserJob userJob) throws SchedulerException;
}
