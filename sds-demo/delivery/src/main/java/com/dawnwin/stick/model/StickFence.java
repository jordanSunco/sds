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
@TableName("stk_fence")
public class StickFence extends Model<StickFence> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer fenceId;

    @TableField(value = "device_id")
    private Integer deviceId;

    @TableField(value = "fence_name")
    private String fenceName;

    private Integer radius;

    @TableField(value = "gps_latitude")
    private Double gpsLatitude;

    @TableField(value = "gps_longitude")
    private Double gpsLongitude;

    @TableField(value = "add_time")
    private Date addTime;

    @TableField(value = "out_alert")
    private Boolean isOutAlert;

    @TableField(value = "in_alert")
    private Boolean isInAlert;

    @TableField(value = "valid")
    private Boolean isValid;

    @Override
    protected Serializable pkVal() {
        return fenceId;
    }
}
