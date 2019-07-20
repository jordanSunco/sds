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
@TableName("stk_heart_blood")
public class StickHeartBlood extends Model<StickHeartBlood> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer heartBloodId;

    @TableField(value = "device_id")
    private Integer deviceId;

    @TableField(value = "heart_rate")
    private Integer heartRate;

    @TableField(value = "blood_pressure")
    private String bloodPressure;

    @DateTimeFormat(pattern= "yyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern= "yyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "add_time")
    private Date addTime;

    @Override
    protected Serializable pkVal() {
        return heartBloodId;
    }
}
