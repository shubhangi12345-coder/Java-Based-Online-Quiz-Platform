package com.mancy.quizplatform.service;

import com.mancy.quizplatform.dto.SettingsForm;
import com.mancy.quizplatform.entity.SystemSetting;
import com.mancy.quizplatform.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads and writes the admin's system settings. A missing or broken value falls back to a safe default. */
@Service
public class SettingsService {

    public static final String REGISTRATION_OPEN = "registration_open";
    public static final String DEFAULT_PASSING_PERCENT = "default_passing_percent";
    private static final int FALLBACK_PASSING_PERCENT = 40;

    private final SystemSettingRepository repository;

    public SettingsService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    public boolean isRegistrationOpen() {
        return repository.findById(REGISTRATION_OPEN)
                .map(setting -> Boolean.parseBoolean(setting.getSettingValue()))
                .orElse(true);
    }

    public int getDefaultPassingPercent() {
        return repository.findById(DEFAULT_PASSING_PERCENT).map(setting -> {
            try {
                int value = Integer.parseInt(setting.getSettingValue().trim());
                return value >= 1 && value <= 100 ? value : FALLBACK_PASSING_PERCENT;
            } catch (NumberFormatException ex) {
                return FALLBACK_PASSING_PERCENT;
            }
        }).orElse(FALLBACK_PASSING_PERCENT);
    }

    public SettingsForm getForm() {
        SettingsForm form = new SettingsForm();
        form.setRegistrationOpen(isRegistrationOpen());
        form.setDefaultPassingPercent(getDefaultPassingPercent());
        return form;
    }

    @Transactional
    public void update(SettingsForm form) {
        repository.save(new SystemSetting(REGISTRATION_OPEN, String.valueOf(form.isRegistrationOpen())));
        repository.save(new SystemSetting(DEFAULT_PASSING_PERCENT, String.valueOf(form.getDefaultPassingPercent())));
    }
}
