package com.simplypos.modules.setting.dto;

import java.util.List;

import com.simplypos.modules.setting.entity.AppSetting;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SettingGroupDto {
    private String groupCode; // LOGIN, SIDEBAR, GENERAL
    private String groupLabel; // "Halaman Login", "Sidebar Brand", "Umum"
    private String groupIcon; // "bi-box-arrow-in-right"
    private List<AppSetting> settings;
}