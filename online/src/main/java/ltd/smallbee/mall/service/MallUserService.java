/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.smallbee.mall.service;

import ltd.smallbee.mall.controller.vo.NewBeeMallUserVO;
import ltd.smallbee.mall.entity.MallUser;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.PageResult;

import javax.servlet.http.HttpSession;

public interface MallUserService {

    /**
     * 登录
     *
     * @param loginName
     * @param passwordMD5
     * @param httpSession
     * @return
     */
    public String login(String loginName, String passwordMD5, HttpSession httpSession);
    /**
     * 用户注册
     *
     * @param loginName
     * @param password
     * @return
     */
    String register(String loginName, String password);
    //验证账号密码
    public MallUser selectByLoginNameAndPasswd(String loginName,String password);
    //添加用户
    public Integer addNewUser(String nickName, String loginName, String password);
    //查找用户
    public MallUser selectUserByLoginName(String loginName);

    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getNewBeeMallUsersPage(PageQueryUtil pageUtil);

    /**
     * 用户信息修改并返回最新的用户信息
     *
     * @param mallUser
     * @return
     */
    NewBeeMallUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession);

    /**
     * 用户禁用与解除禁用(0-未锁定 1-已锁定)
     *
     * @param ids
     * @param lockStatus
     * @return
     */
    Boolean lockUsers(Integer[] ids, int lockStatus);


}
