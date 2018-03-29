package priv.llf.job.serve.utils;

import priv.llf.job.client.dto.UserJobDetailDto;
import priv.llf.job.serve.dto.UserJob;

/**
 * @Author: Eleven
 * @Since: 2018/3/29 23:21
 * @Description:
 */
public class UserJobFactory {

    public static  UserJob buildUserJob(UserJobDetailDto userJodDetail){

        UserJob us = new UserJob();


        return us;
    }
}
