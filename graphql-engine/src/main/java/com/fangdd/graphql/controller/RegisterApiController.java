package com.fangdd.graphql.controller;

import com.fangdd.graphql.provider.dto.BaseResponse;
import com.fangdd.graphql.provider.dto.TpDocGraphqlProviderServiceInfo;
import com.fangdd.graphql.register.GraphqlRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xuwenzhen
 * @chapter Graphql引擎
 * @section Provider注册接口
 * @date 2019/4/9
 */
@RestController
@RequestMapping("/api/register")
public class RegisterApiController {
    @Autowired
    private GraphqlRegister<TpDocGraphqlProviderServiceInfo> graphqlRegister;

    /**
     * 注册远端数据供应商（Duo-Doc项目）
     *
     * @return
     */
    @PostMapping("/tpdoc")
    public BaseResponse registerWithTpDoc(@RequestBody TpDocGraphqlProviderServiceInfo request) {
        graphqlRegister.register(request);
        return BaseResponse.success();
    }

    /**
     * Ping
     * @return
     */
    @GetMapping("/ping")
    public BaseResponse<String> ping() {
        return BaseResponse.success("success");
    }
}
