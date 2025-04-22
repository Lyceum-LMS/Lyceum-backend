package com.cst438.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

@RestController
public class HealthCheckController {

    @Autowired
    private ApplicationContext appContext;

    /*
     * health check
     */
    @GetMapping("/check")
    public String healthCheck() {
        try {
            String ip = InetAddress.getLocalHost().toString();
            long pid = ProcessHandle.current().pid();
            return ip+" pid="+pid;
        } catch (UnknownHostException e) {
            return "unknown ip";
        }
    }
    /*
     * terminate the server
     */
    @GetMapping("/fail")
    public void fail() {
        SpringApplication.exit(appContext, () -> 1);	}
}
