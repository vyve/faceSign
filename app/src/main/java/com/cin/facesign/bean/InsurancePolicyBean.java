package com.cin.facesign.bean;

import lombok.Data;

/**
 * 保单详情
 * Created by 王新超 on 2020/6/17.
 */
@Data
public class InsurancePolicyBean {
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 投保人
     */
    private String policyHolderName;
    /**
     * 被保险人
     */
    private String insuredName;
    /**
     * 贷款金额
     */
    private String amount;

    private String createdTime;
}
