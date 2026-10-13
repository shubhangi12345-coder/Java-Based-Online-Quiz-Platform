package com.mancy.quizplatform.dto;

import jakarta.validation.constraints.*;
import com.mancy.quizplatform.enums.*;

/**
 * Data typed in the admin System Settings form.
 */
public class SettingsForm {

    private boolean registrationOpen;

    @NotNull(message = "Passing percentage is required")
    @Min(value = 1, message = "Must be at least 1")
    @Max(value = 100, message = "Cannot exceed 100")
    private Integer defaultPassingPercent;

    public boolean isRegistrationOpen() {
        return registrationOpen;
    }

    public void setRegistrationOpen(boolean registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    public Integer getDefaultPassingPercent() {
        return defaultPassingPercent;
    }

    public void setDefaultPassingPercent(Integer defaultPassingPercent) {
        this.defaultPassingPercent = defaultPassingPercent;
    }

}
