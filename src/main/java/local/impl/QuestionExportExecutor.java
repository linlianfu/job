package local.impl;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import priv.llf.job.client.dto.response.UserJobExecuteResult;
import priv.llf.job.client.support.UserJobExecutor;
import priv.llf.job.serve.dto.args.UserJob;

/**
 * @Author: Eleven
 * @Since: 2018/4/1 11:42
 * @Description:  异步任务执行类，继承UserJobExecutor，执行execute方法
 */
@Slf4j
public class QuestionExportExecutor implements UserJobExecutor {
    @Override
    public UserJobExecuteResult execute(UserJob userJob, JobDataMap jobDataMap) {
        UserJobExecuteResult result = new UserJobExecuteResult();
        log.info("导出试题异步任务任务开始执行。。。。");
        return result;
    }
}
