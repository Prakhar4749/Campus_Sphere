package com.notificationService.clients;

import com.notificationService.DTO.HodDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "admin-service", path = "/admin") // Use the name registered in Eureka
public interface AdminClient {

    @GetMapping("/departments/{departmentId}/hod")
    HodDetailsResponse getHodDetails(@PathVariable("departmentId") Long departmentId);
}