package priv.llf.job.serve.dto.args;

import lombok.Data;
import org.quartz.JobDataMap;
import org.quartz.JobKey;

import java.io.Serializable;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 22:18
 * @Description: 服务内部异步任务参数，将client的UserJodDetailDto转为该dto
 *
 * 将该model持久化到数据库
 */
@Data
public class UserJob implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * 任务名
     */
    private String name;
    /**
     * 任务分组
     */
    private String group;
    /**
     * 任务marker
     */
    private String remark;
    /**
     *
     */
    private boolean durable=true;
    /**
     * 任务执行失败或者挂机情况下，当恢复正常时候是否重新执行任务
     */
    private boolean requestsRecovery;
    /**
     * 实现类，回调类
     */
    private String jobClassName;

    private String description;

    private JobDataMap jobDataMap=new JobDataMap();

    public JobKey getJobKey(){
        return JobKey.jobKey(name, group);
    }

}
