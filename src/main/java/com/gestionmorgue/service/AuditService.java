package com.gestionmorgue.service;

import com.gestionmorgue.dao.AuditLogDao;
import com.gestionmorgue.model.AuditLog;

import java.util.List;

public class AuditService {
    private final AuditLogDao dao;

    public AuditService() {
        this.dao = new AuditLogDao();
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return dao.findRecent(limit);
    }

    public long getLogCount() {
        return dao.count();
    }
}
