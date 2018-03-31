package priv.llf.job.client.dto.args;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.quartz.JobDataMap;
import priv.llf.job.client.support.UserJobExecutor;

import javax.validation.constraints.NotNull;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 22:14
 * @Description:
 */
@Data
public class UserJobDetailDto {
    /**
     * 任务的描述
     */
    private String description;
    @NotNull(message="Job的实现方式不能为空")
    private Class<? extends UserJobExecutor> jobClass;
    @NotBlank(message="Job的名称不能为空")
    private String name;
    @NotBlank(message="Job的分组不能为空")
    private String group;
    private String remark;
    private transient volatile JobDataMap jobDataMap=new JobDataMap();

}
