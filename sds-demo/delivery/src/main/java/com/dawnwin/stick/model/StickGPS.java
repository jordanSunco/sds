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
    private Integer gpsId;

    @TableField(value = "device_id")
    private Integer deviceId;

    @TableField(value = "location_type")
    private String locationType;

    private Double latitude;

    private Double longitude;

    private String direction;

    private String address;

    private String speed;

    private String satellite;

    private String signal;

    private String battery;

    @TableField(value = "gps_time")
    private Date gpsTime;

    @TableField(value = "gps_data")
    private String gpsData;

    @Override
    protected Serializable pkVal() {
        return gpsId;
    }
}
