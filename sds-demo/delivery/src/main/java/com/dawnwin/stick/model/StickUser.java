package com.dawnwin.stick.model;


import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("stk_user")
public class StickUser extends Model<StickUser> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private int userId;

    private String mobile;

    private String password;

    private int love;

    @TableField(value = "nick_name")
    private String nickName;

    @TableField(value = "add_time")
    private Date addTime;

    @TableField(value = "last_login_time")
    private Date lastLoginTime;

    @Override
    protected Serializable pkVal() {
        return userId;
    }
}
