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
@TableName("stk_gps")
public class StickGPS extends Model<StickGPS> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private String gpsId;

    @TableField(value = "device_id")
    private int deviceId;

    @TableField(value = "location_type")
    private String locationType;

    private double latitude;

    private double longitude;

    private String direction;

    private String address;

    private String speed;

    private String satellite;

    private String signal;

    private String battery;

    @TableField(value = "gps_time")
    private Date gpsTime;

    @Override
    protected Serializable pkVal() {
        return gpsId;
    }
}
