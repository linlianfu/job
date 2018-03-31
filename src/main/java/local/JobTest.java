package local;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import priv.llf.job.client.dto.args.UserJobDetailDto;
import priv.llf.job.client.service.UserJobClient;


/**
 * @Author: Eleven
 * @Since: 2018/3/29 20:54
 * @Description:
 */
@Slf4j
@Controller
@RequestMapping("job")
public class JobTest {

    @Autowired
    UserJobClient asynchronousJobClient;

    @ResponseBody
    @RequestMapping("startJob")
    public void startJob(){
        UserJobDetailDto userJobDetailDto = new UserJobDetailDto();
        try {
            asynchronousJobClient.execute(userJobDetailDto);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        log.info("job测试启动");
    }
}
