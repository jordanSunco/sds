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
@TableName("stk_user_device")
public class StickUserDevice extends Model<StickUserDevice> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer bindId;

    @TableField(value = "user_id")
    private Integer userId;

    @TableField(value = "device_id")
    private Integer deviceId;

    @TableField(value = "bind_type")
    private Integer bindType;

    @TableField(value = "add_time")
    private Date addTime;

    @TableField(value = "is_default")
    private Boolean userDefault;

    @Override
    protected Serializable pkVal() {
        return bindId;
    }
}
