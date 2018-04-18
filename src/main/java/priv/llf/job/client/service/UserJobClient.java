package priv.llf.job.client.service;

import org.quartz.SchedulerException;
import org.springframework.validation.annotation.Validated;
import priv.llf.job.client.dto.args.UserJobDetailDto;

import javax.validation.Valid;

/**
 * @Author: Eleven
 * @Since: 2018/3/10 11:04
 * @Description:
 */
@Validated
public interface UserJobClient {
    /**
     * 执行一个任务
     * @param userJobDetailDto
     */
    void execute(@Valid UserJobDetailDto userJobDetailDto) throws SchedulerException;
}
