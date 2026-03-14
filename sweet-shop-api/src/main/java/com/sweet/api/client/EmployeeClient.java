package com.sweet.api.client;

import com.sweet.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(value = "sweet-shop-user", path = "/admin", contextId = "adminClient")
public interface EmployeeClient {

    @GetMapping("/employee/riderIds")
    Result<List<Long>> getRiderIds();
}
