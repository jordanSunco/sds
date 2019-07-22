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
@TableName("stk_warn")
public class StickWarn extends Model<StickWarn> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer warnId;

    @TableField(value = "device_id")
    private Integer deviceId;

    @TableField(value = "warn_type")
    private Integer warnType;

    private String content;

    @DateTimeFormat(pattern= "yyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern= "yyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "warn_time")
    private Date warnTime;

    @Override
    protected Serializable pkVal() {
        return warnId;
    }
}
