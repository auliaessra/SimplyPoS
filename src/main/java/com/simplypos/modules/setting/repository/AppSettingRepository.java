package com.simplypos.modules.setting.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.simplypos.modules.setting.entity.AppSetting;

public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {

    Optional<AppSetting> findBySettingKey(String settingKey);

    List<AppSetting> findBySettingGroupOrderBySortOrderAsc(String settingGroup);

    List<AppSetting> findAllByOrderBySettingGroupAscSortOrderAsc();

    @Query("SELECT s FROM AppSetting s WHERE s.isPublic = true")
    List<AppSetting> findAllPublic();

    boolean existsBySettingKey(String settingKey);
}