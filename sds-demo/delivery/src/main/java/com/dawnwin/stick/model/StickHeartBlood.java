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

    @TableField(value = "add_time")
    private Date addTime;

    @Override
    protected Serializable pkVal() {
        return heartBloodId;
    }
}
