package com.innowise.identity_audit_compliance_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods = false)
public final class IdentityAuditComplianceServiceApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args
     *            command line arguments
     */
    public static void main(final String[] args) {
        Class<?> appClass = IdentityAuditComplianceServiceApplication.class;
        SpringApplication.run(appClass, args);
    }

    private IdentityAuditComplianceServiceApplication() {
        super();
    }
}
