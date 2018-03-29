package local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @Author: Eleven
 * @Since: 2018/3/29 20:54
 * @Description:
 */
@Slf4j
@Controller
@RequestMapping("job")
public class JobTest {

    @ResponseBody
    @RequestMapping("startJob")
    public void startJob(){
        log.info("job测试启动");
    }
}
