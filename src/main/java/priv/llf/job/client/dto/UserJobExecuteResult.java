package priv.llf.job.client.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @Author: calvin
 * @Since: 2018/3/29 22:37
 * @Description:
 */
@Data
public class UserJobExecuteResult {

    /**
     * 标记任务业务上是否执行成功
     */
    private boolean success;
    /**
     * 任务执行成功或失败的信息
     */
    private String message;
    /**
     * 任务执行的返回结果
     */
    private JSONObject result;



    public enum  Code{
    }

}
