package priv.llf.job.client.service;

import org.quartz.SchedulerException;
import priv.llf.job.client.dto.UserJobDetailDto;

/**
 * @Author: Eleven
 * @Since: 2018/3/10 11:04
 * @Description:
 */
public interface UserJobClient {
    /**
     * 执行一个任务
     * @param userJobDetailDto
     */
    void execute(UserJobDetailDto userJobDetailDto) throws SchedulerException;
}
