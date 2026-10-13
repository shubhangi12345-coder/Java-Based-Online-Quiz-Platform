package com.mancy.quizplatform.repository;

import com.mancy.quizplatform.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
