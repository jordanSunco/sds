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

    @TableField(value = "avaster")
    private String avaster;

    @TableField(value = "nickname")
    private String nickName;

    @DateTimeFormat(pattern= "yyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern= "yyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "add_time")
    private Date addTime;

    private String sex;

    private Integer weight;

    private Integer height;

    private Integer age;

    private String city;

    @TableField(value = "sos_list")
    private String sosList;

    @TableField(value = "switch_on_off")
    private Integer switchOnOff;

    @DateTimeFormat(pattern= "yyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern= "yyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bindTime;

    private Integer bindType;

    private Boolean isDefault;

    @Override
    protected Serializable pkVal() {
        return deviceId;
    }

    public void reset(){
        avaster = null;
        bindPhone = null;
        nickName = null;
        sex = null;
        weight = null;
        height = null;
        age = null;
        city = null;
        sosList = null;
        updateById();
    }
}
