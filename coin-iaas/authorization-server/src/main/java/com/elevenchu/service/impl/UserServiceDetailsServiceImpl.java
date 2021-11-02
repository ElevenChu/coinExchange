package com.elevenchu.service.impl;

import com.elevenchu.constant.LoginConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String loginType = requestAttributes.getRequest().getParameter("login_type");//区分是后台管理人员还是普通用户
        if(StringUtils.isEmpty(loginType)){
            throw new AuthenticationServiceException("登录类型不能为null");

        }
        UserDetails userDetails=null;
       try {
           switch (loginType) {
               case LoginConstant.ADMIN_TYPE:
                   userDetails =loadSysUserByUsername(username);
                    break;
               case LoginConstant.MEMEBER_TYPE:
                   userDetails= loadMemberUserByUsername(username);
                   break;
               default:
                   throw new AuthenticationServiceException("暂不支持的登录方式" + loginType);

           }
       }catch (IncorrectResultSizeDataAccessException e){
           throw new UsernameNotFoundException("用户名"+username+"不存在");
       }

        return  userDetails;
    }

    /**
     * 会员登录
     * @param username
     * @return
     */

    private UserDetails loadMemberUserByUsername(String username) {
    return null;
    }


    /**
     * 后台管理人员登录
     * @param username
     * @return
     */
    private UserDetails loadSysUserByUsername(String username) {

       //1.使用用户名查询用户
     return  jdbcTemplate.queryForObject(LoginConstant.QUERY_ADMIN_SQL, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet resultSet, int i) throws SQLException {

                if(resultSet.wasNull()){
                    throw new UsernameNotFoundException("用户名"+username+"不存在");
                }
                long id = resultSet.getLong("id");//用户ID
                String password = resultSet.getString("password");//用户密码
                int status = resultSet.getInt("status");

                //3.封装成一个UserDetails对象并返回
                return new User(
                        String.valueOf(id),//使用ID代替username
                        password,
                        status==1,
                        true,
                        true,
                        true,
                        getSysUserPermission(id)
                        
                );
            }

     },username);



    }

    /**
     * //2.通过用户的ID查询用户的权限
     * @param id
     * @return
     */
    private Collection<? extends GrantedAuthority> getSysUserPermission(long id) {
        String roleCode = jdbcTemplate.queryForObject(LoginConstant.QUERY_ROLE_CODE_SQL, String.class);
        List<String> permissions=null;//权限的名称
        //1.当用户为超级管理员时，拥有所有权限
      if(LoginConstant.ADMIN_ROLE_CODE.equals(roleCode)){
          permissions = jdbcTemplate.queryForList(LoginConstant.QUERY_ALL_PERMISSIONS,String.class);

      }else{
          //2.普通用户，需要使用角色->权限数据
         permissions = jdbcTemplate.queryForList(LoginConstant.QUERY_PERMISSION_SQL, String.class);

      }
      if (permissions==null||permissions.isEmpty()){
          return Collections.emptySet();
      }

      return permissions.stream()
              .distinct()//去重
              .map(perm->new SimpleGrantedAuthority(perm))
              .collect(Collectors.toSet());




    }
}
