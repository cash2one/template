package com.br.template.service;

import com.br.template.mapper.UserMapper;
import com.br.template.constent.ResultStatus;
import com.br.template.model.User;
import com.br.template.constent.UserRole;
import com.br.template.util.MdImplement;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author xin.cao@100credit.com
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    public List<User> selectAllSelective(User user) {
        return userMapper.selectAllSelective(user);
    }

    public User selectSelective(User user) {
        return userMapper.selectSelective(user);
    }

    /**
     * 添加用户（普通角色）
     *
     * @param user
     * @return
     */
    public boolean createUser(User user) {
        if (user.againPasswdOk() && !haveAccount(user.getAccount()) && !haveEmail(user.getEmail())) {
            user.setPasswd(MdImplement.encodeMD5To32(user.getPasswd().toLowerCase()));
            user.setUserRole(UserRole.COMMON.getId());
            user.setValied(ResultStatus.YES.getNo());
            userMapper.insertSelective(user);
            return true;
        }
        return false;
    }

    /**
     * 身份识别
     *
     * @param user
     * @return
     */
    public boolean loginCheck(User user) {
        boolean isOk = false;
        User realUser = getUserByAccount(user.getAccount());
        if (realUser != null) {
            String pass = realUser.getPasswd();
            if (user.getPasswd().trim().equalsIgnoreCase(pass) && ResultStatus.isYES(realUser.getValied())) {
                isOk = true;
                user.setUserRole(realUser.getUserRole());
                user.setPlatformId(realUser.getPlatformId());
            }
        }

        return isOk;
    }

    public void addBlacklist(Integer platformId, String account) {
        User user = getUserByPlatformIdAndAccount(platformId, account);
        if (user != null) {
            user.setValied(ResultStatus.NO.getNo());
            userMapper.updateByPrimaryKeySelective(user);
        }
    }

    public void changePasswd(Integer platformId, String account, String passwd) {
        User user = getUserByPlatformIdAndAccount(platformId, account);
        if (user != null) {
            user.setPasswd(MdImplement.encodeMD5To32(passwd.trim()));
            userMapper.updateByPrimaryKeySelective(user);
        }
    }

    public User getUserByAccount(String account) {
        User user = new User();
        user.setAccount(account);
        return selectSelective(user);
    }

    public User getUserByPlatformIdAndAccount(Integer platformId, String account) {
        User user = new User();
        user.setPlatformId(platformId);
        user.setAccount(account);
        return selectSelective(user);
    }

    public User getUserByEmail(String email) {
        User user = new User();
        user.setEmail(email);
        return selectSelective(user);
    }

    public boolean haveAccount(String account) {
        return getUserByAccount(account) != null;
    }

    public boolean haveEmail(String email) {
        return getUserByEmail(email) != null;
    }
}
