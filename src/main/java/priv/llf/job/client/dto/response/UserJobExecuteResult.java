package priv.llf.job.client.dto.response;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 22:37
 * @Description:
 */
@Data
@NoArgsConstructor
public class UserJobExecuteResult {

    /**
     * 标记任务业务上是否执行成功
     */
    private String code = Code.SUCCESS.getValue();
    /**
     * 任务执行成功或失败的信息
     */
    private String message;
    /**
     * 任务执行的返回结果
     */
    private JSONObject result;

    public UserJobExecuteResult(String code,String message){

        this.code = code;

        this.message = message;

    }

    public enum  Code{

        SUCCESS("500"),

        WARN("505"),

        ERROR("400");

        @Setter
        private String value;

        public String getValue(){
            return this.value;
        }

        Code(String value){
            this.value = value;
        }

    }

}
