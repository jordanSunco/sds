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
    private String fenceId;

    @TableField(value = "device_id")
    private int deviceId;

    @TableField(value = "fence_name")
    private String fenceName;

    private int radius;

    @TableField(value = "gps_latitude")
    private String gpsLatitude;

    @TableField(value = "gps_longitude")
    private String gpsLongitude;

    @TableField(value = "add_time")
    private Date addTime;

    @Override
    protected Serializable pkVal() {
        return fenceId;
    }
}
