package local.gateway;

import local.impl.QuestionExportExcutor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
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
    @RequestMapping("exportQuestion")
    public void export(){
        UserJobDetailDto userJobDetailDto = new UserJobDetailDto();
        userJobDetailDto.setJobClass(QuestionExportExcutor.class);
        JobDataMap map = new JobDataMap();
        map.put("group","jobTest");
        userJobDetailDto.setJobDataMap(map);
        userJobDetailDto.setName("导出试题");
        userJobDetailDto.setGroup("questionExport");
        userJobDetailDto.setDescription("测试阶段");
        userJobDetailDto.setRemark("remark");
        try {
            asynchronousJobClient.execute(userJobDetailDto);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
