package com.elevenchu.controller;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class FlowRulesController {



    @GetMapping("/gateway")
    public Set<GatewayFlowRule> getGatewayFlowRules(){
        //test-------------
        //2
        int x = 0;
        return GatewayRuleManager.getRules();
    }

    @GetMapping("/api")
    public Set<ApiDefinition> getApiGroupRules(){
        return GatewayApiDefinitionManager.getApiDefinitions();
    }

}
