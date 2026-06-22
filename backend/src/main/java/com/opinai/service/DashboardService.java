package com.opinai.service;

import com.opinai.controller.dto.DashboardOverviewResponse;

public interface DashboardService {
    DashboardOverviewResponse getDashboardOverview(String userEmail);
}
