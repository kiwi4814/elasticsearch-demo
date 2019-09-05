package com.example.elasticsearch.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseInfo {
    /**
     * 主键 L_ID
     */
    private String id;
    /**
     * 投资ID VC_INVEST_ID
     */
    private String investId;
    /**
     * 项目名称 VC_PROJECT_NAME
     */
    private String projectName;
    /**
     * 项目简称 VC_SHORT_NAME
     */
    private String shortName;
    /**
     * 项目编号 VC_PROJECT_CODE
     */
    private String projectCode;

    /**
     * 统一社会信用代码 VC_USCI
     */
    private String usci;
    /**
     * 投资轮次 E_INVEST_ROTATION
     */
    private String investRotation;
    /**
     * 投资类型 E_INVEST_TYPE
     */
    private String investType;
    /**
     * 参考币种 E_BASE_CURRENCY
     */
    private String baseCurrency;
    /**
     * 项目来源 E_SOURCE
     */
    private String source;
    /**
     * 股票代码 VC_STOCK_CODE
     */
    private String stockCode;
    /**
     * 办公地址 VC_BUSINESS_ADDRESS
     */
    private String businessAddress;
    /**
     * 是否属于中小企业 E_IS_SME
     */
    private String isSme;
    /**
     * 是否属于高新技术企业 E_IS_HNTE
     */
    private String isHnte;
    /**
     * 是否属于初创科技型企业 E_IS_SUE
     */
    private String isSue;
    /**
     * 是否享受国家财税政策 E_IS_ENFATP
     */
    private String isEnfatp;
    /**
     * 商业模式 VC_BUSINESS_MODEL
     */
    private String businessModel;
    /**
     * 其他说明 VC_COMMENT
     */
    private String comment;
    /**
     * 阶段ID L_STAGE_ID
     */
    private String stageId;
    /**
     * 模板ID L_STAGE_TEMPLATE_ID
     */
    private String stageTemplateId;
    private String investStatus;
    private String isInvestAfter;
    private String isExit;
    /**
     * 新增用户 VC_CREATE_USER
     */
    private String createUser;
    /**
     * 新增时间 D_CREATE_TIME
     */
    private String createTime;
    /**
     * 修改用户 VC_UPDATE_USER
     */
    private String updateUser;
    /**
     * 修改时间 D_UPDATE_TIME
     */
    private String updateTime;

}

