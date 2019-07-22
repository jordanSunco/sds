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
@TableName("stk_device")
public class StickDevice extends Model<StickDevice> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer deviceId;

    @TableField(value = "device_imei")
    private String deviceImei;

    @TableField(value = "bind_phone")
    private String bindPhone;

    @TableField(value = "user_id")
    private Integer userId;

    @TableField(value = "avaster")
    private String avaster;

    @TableField(value = "nickname")
    private String nickName;

    @TableField(value = "add_time")
    private Date addTime;

    @TableField(value = "user_default")
    private Boolean userDefault;

    private String sex;

    private Integer weight;

    private Integer height;

    private Integer age;

    private String city;

    @TableField(value = "sos_list")
    private String sosList;

    @TableField(value = "bind_time")
    private Date bindTime;

    @TableField(value = "switch_on_off")
    private Integer switchOnOff;

    @Override
    protected Serializable pkVal() {
        return deviceId;
    }
}
