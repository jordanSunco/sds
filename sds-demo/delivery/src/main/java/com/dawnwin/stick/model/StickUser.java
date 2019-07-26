package com.dawnwin.stick.model;


import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("stk_user")
public class StickUser extends Model<StickUser> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer userId;

    private String mobile;

    private String password;

    private Integer love;

    @TableField(value = "nick_name")
    private String nickName;

    @DateTimeFormat(pattern= "yyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern= "yyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "add_time")
    private Date addTime;

    @DateTimeFormat(pattern= "yyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern= "yyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "last_login_time")
    private Date lastLoginTime;

    @Override
    protected Serializable pkVal() {
        return userId;
    }
}
