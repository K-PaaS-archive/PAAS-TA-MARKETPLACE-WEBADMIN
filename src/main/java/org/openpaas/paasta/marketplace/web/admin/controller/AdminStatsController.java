package org.openpaas.paasta.marketplace.web.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpaas.paasta.marketplace.api.domain.*;
import org.openpaas.paasta.marketplace.web.admin.common.CommonService;
import org.openpaas.paasta.marketplace.web.admin.service.AdminCategoryService;
import org.openpaas.paasta.marketplace.web.admin.service.AdminSellerProfileService;
import org.openpaas.paasta.marketplace.web.admin.service.AdminSoftwareService;
import org.openpaas.paasta.marketplace.web.admin.service.AdminStatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author hrjin
 * @version 1.0
 * @since 2019-08-30
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminSoftwareService adminSoftwareService;
    private final AdminSellerProfileService adminSellerProfileService;
    private final AdminStatsService adminStatsService;
    private final AdminCategoryService adminCategoryService;
    private final CommonService commonService;

    /**
     * 상품별 현황 메인페이지로 이동한다.
     *
     * @return ModelAndView(Spring 클래스)
     */
    @GetMapping(value = "/softwares")
    public String getSoftwareStatsMain(Model model, HttpServletRequest httpServletRequest) {
        CustomPage<Software> software = adminSoftwareService.getAdminSoftwareList(commonService.setParameters(httpServletRequest));

        List<Long> idIn = new ArrayList<>();

        for (Software s:software.getContent()) {
            idIn.add(s.getId());
        }

        model.addAttribute("categories", adminSoftwareService.getAdminCategories());
        model.addAttribute("spec", new SoftwareSpecification());

        Map<Long, Long> result = adminStatsService.getCountsOfInsts(idIn);
        Map newResult = new HashMap();

        for (Long id:idIn) {
            String mapId = "" + id;
            if(result.get(mapId) != null){
                newResult.put(mapId, result.get(mapId));
            }else{
                newResult.put(mapId, 0);
            }
        }

        //사용량 추이
        Map  countsOfInstsProvider =  adminStatsService.countsOfInstsProviderMonthlyTransition(idIn);
        model.addAttribute("totalCountInstsProviderInfo", commonService.getJsonStringFromMap(countsOfInstsProvider));
        model.addAttribute("countOfInstsProviderMonthly", countsOfInstsProvider.get("terms"));
        model.addAttribute("countOfInstsProviderCounts", countsOfInstsProvider.get("counts"));

        model.addAttribute("instanceUserCount", commonService.getJsonStringFromMap(newResult));
        model.addAttribute("instanceCountSum", adminStatsService.getCountOfInstsUsing());
        model.addAttribute("instanceUsingUserSum", adminStatsService.getCountOfUsersUsing());
        return "contents/useStatusSoftware";
    }


    /**
     * 상품별 현황 상세 페이지
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping(value = "/softwares/{id}")
    public String getSoftwareStats(Model model, @PathVariable Long id,  HttpServletRequest httpServletRequest) {
        model.addAttribute("softwareStat", adminSoftwareService.getAdminSoftwares(id));

        // 단일 상품에 대한 총 사용자 수
        List<Long> idIn = new ArrayList<>();
        idIn.add(id);

        Map<Long, Long> result = adminStatsService.getCountsOfInsts(idIn);
        Iterator iter = result.keySet().iterator();
        Object usedSwCount = null;

        if(result.size() > 0) {
            while(iter.hasNext()){
                Object key = iter.next();
                usedSwCount = result.get(key);
            }
        }else {
            usedSwCount = 0;
        }

        //사용량 추이
        CustomPage<Software> software = adminSoftwareService.getAdminSoftwareList(commonService.setParameters(httpServletRequest));
        List<Long> softwareId = new ArrayList<>();
        for (Software s:software.getContent()) {
            softwareId.add(s.getId());
        }

        Map  countsOfInstsMonthly =  adminStatsService.countsOfInstsProviderMonthlyTransition(idIn);
        model.addAttribute("totalCountsOfInstsMonthlyInfo", commonService.getJsonStringFromMap(countsOfInstsMonthly));
        model.addAttribute("countsOfInstsMonthly", countsOfInstsMonthly.get("terms"));
        model.addAttribute("ccountsOfInstsMonthlyCounts", countsOfInstsMonthly.get("counts"));

        model.addAttribute("usedSwCountSum", usedSwCount);

        return "contents/useStatusSoftwareDetail";
    }


    /**
     * 상품별 현황 상세 페이지 - 판매 현황 목록 조회
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping(value = "/instances")
    @ResponseBody
    public CustomPage<Instance> getInstanceListBySwId(Model model, HttpServletRequest httpServletRequest) {
        //사용량 추이
        Map  countsOfInstsProvider =  adminStatsService.countsOfInstsProviderMonthly();
        model.addAttribute("countOfInstsProviderMonthly", countsOfInstsProvider.get("terms"));
        model.addAttribute("countOfInstsProviderCounts", countsOfInstsProvider.get("counts"));
        return adminStatsService.getInstanceListBySwId(commonService.setParameters(httpServletRequest));
    }


    /**
     * 판매자별 현황 메인페이지로 이동한다.
     *
     * @return ModelAndView(Spring 클래스)
     */
    @GetMapping(value = "/sellers")
    public String getSellerStatsMain(Model model, HttpServletRequest httpServletRequest) {

        CustomPage<Profile> profileList = adminSellerProfileService.getProfileList(commonService.setParameters(httpServletRequest));

        List<String> idIn = new ArrayList<>();
        for (Profile f:profileList.getContent()) {
            idIn.add(f.getId());
        }

        model.addAttribute("profiles",adminSellerProfileService.getProfiles());

        // 판매자 총 건수
        Map<String, Long> totalSoldResult = adminStatsService.getCountsOfInstanceProvider();
        model.addAttribute("soldSoftwareCount", commonService.getJsonStringFromMap(commonService.getResultMap(idIn, totalSoldResult)));

        // 승인 상품 수
        Map<String, Long> totalApprovalSwCount = adminStatsService.getCountsOfSwsProvider();
        model.addAttribute("approvalSoftwareCount", commonService.getJsonStringFromMap(commonService.getResultMap(idIn, totalApprovalSwCount)));

        // 총 판매량
        model.addAttribute("instanceCountSum", adminStatsService.getCountOfInstsUsing());

        //사용량 추이(month)
        Map countsOfInstsProvider =  adminStatsService.countsOfInstsProviderMonthly();
        model.addAttribute("totalCountInstsProviderInfo", commonService.getJsonStringFromMap(countsOfInstsProvider));
        model.addAttribute("countOfInstsProviderMonthly", countsOfInstsProvider.get("terms"));
        model.addAttribute("countOfInstsProviderCounts", countsOfInstsProvider.get("counts"));

        return "contents/useStatusSeller";
    }

    /**
     * 판매자별 현황 상세페이지로 이동한다.
     *
     * @return ModelAndView(Spring 클래스)
     */
    @GetMapping(value = "/sellers/{id}")
    public String getSellerStats(Model model, @PathVariable String id, HttpServletRequest httpServletRequest) {

        model.addAttribute("categories", adminSoftwareService.getAdminCategories());
        model.addAttribute("sellerStat", adminSellerProfileService.getProfiles(id));

        // 단일 상품에 대한 총 사용자 수
        CustomPage<Profile> profileList = adminSellerProfileService.getProfileList(commonService.setParameters(httpServletRequest));

        List<String> idIn = new ArrayList<>();
        for (Profile f:profileList.getContent()) {
            idIn.add(f.getId());
        }

        // 판매자 총 건수
        Map<String, Long> totalSoldResult = adminStatsService.getCountsOfInstanceProvider();
        model.addAttribute("soldSoftwareCount", commonService.getJsonStringFromMap(commonService.getResultMap(idIn, totalSoldResult)));

        //승인(status = Approval)
        CustomPage<Software> sellerMySoftware = adminSoftwareService.getAdminSoftwareList("?createdBy=" + id);
        List<Long> softwareId = new ArrayList<>();
        for (Software s:sellerMySoftware.getContent()) {
            softwareId.add(s.getId());
        }
        model.addAttribute("instanceUserCountProvider", adminStatsService.getUsingPerInstanceByProvider(id, softwareId));

        //사용량 추이
        Map  countsOfInstsProvider =  adminStatsService.countsOfInstsProviderMonthlyTransition(softwareId);
        model.addAttribute("totalCountInstsProviderInfo", commonService.getJsonStringFromMap(countsOfInstsProvider));
        model.addAttribute("countOfInstsProviderMonthly", countsOfInstsProvider.get("terms"));
        model.addAttribute("countOfInstsProviderCounts", countsOfInstsProvider.get("counts"));

        return "contents/useStatusSellerDetail";
    }


    /**
     * 사용자별 현황메인페이지로 이동한다.
     *
     * @return ModelAndView(Spring 클래스)
     */
    @GetMapping(value = "/users/list")
    public String getUserStatsMain(Model model, HttpServletRequest httpServletRequest) {
        CustomPage<Profile> profileList = adminSellerProfileService.getProfileList(commonService.setParameters(httpServletRequest));

        List<String> createdBy = new ArrayList<>();

        for (Profile f:profileList.getContent()) {
            createdBy.add(f.getId());
        }

        Map<String, Long> result = adminStatsService.getCountsOfInstsUser(createdBy);
        Map newResult = new HashMap();

        for (String id:createdBy) {
            String mapId = "" + id;
            if(result.get(mapId) != null){
                newResult.put(mapId, result.get(mapId));
            }else{
                newResult.put(mapId, 0);
            }
        }

        //사용량 추이
        Map  countsOfUserProvider =  adminStatsService.countsOfInstsUserMonthly(createdBy);
        model.addAttribute("totalCountUserProviderInfo", commonService.getJsonStringFromMap(countsOfUserProvider));
        model.addAttribute("countsOfUserProviderMonthly", countsOfUserProvider.get("terms"));
        model.addAttribute("countsOfUserProviderCounts", countsOfUserProvider.get("counts"));

        // 사용자별 구매 상품 수
        model.addAttribute("instancesCount", commonService.getJsonStringFromMap(adminStatsService.countsOfInstsUser()));

        return "contents/useStatusUser";
    }


    /**
     * 사용자별 상세 페이지 이동 및 조회
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping(value = "/users/{id}")
    public String getUserStats(Model model, @PathVariable String id, HttpServletRequest httpServletRequest) {

        model.addAttribute("userStat", adminStatsService.getUser(id));
        model.addAttribute("categories", adminCategoryService.getCategoryList());
        model.addAttribute("spec", new SoftwareSpecification());
        model.addAttribute("status", Software.Status.values());
        model.addAttribute("instancesCount", commonService.getJsonStringFromMap(adminStatsService.countsOfInstsUser()));

        CustomPage<Profile> profiles = adminSellerProfileService.getProfileList("?createdBy=" + id);
        List<String> createdBy = new ArrayList<>();
        for (Profile f:profiles.getContent()) {
            createdBy.add(f.getId());
        }

        Map countsOfUserProvider =  adminStatsService.countsOfInstsUserMonthly(createdBy);
        model.addAttribute("totalCountUserProviderInfo", commonService.getJsonStringFromMap(countsOfUserProvider));
        model.addAttribute("countsOfUserProviderMonthly", countsOfUserProvider.get("terms"));
        model.addAttribute("countsOfUserProviderCounts", countsOfUserProvider.get("counts"));

        return "contents/useStatusUserDetail";
    }


    /**
     * 사용자별 상품 상세 페이지(전체상품수)
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping(value = "/instances/users")
    @ResponseBody
    public CustomPage<Instance> getInstanceListBySwInId(Model model, HttpServletRequest httpServletRequest) {
        return adminStatsService.getInstanceListBySwInId(commonService.setParameters(httpServletRequest));
    }


    /**
     * Admin 상품 목록 조회
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping(value = "/sellers/registSwList")
    @ResponseBody
    public CustomPage<Software> getSoftwaresPerSellerList(HttpServletRequest httpServletRequest){
        return adminSoftwareService.getAdminSoftwareList(commonService.setParameters(httpServletRequest));
    }


    /**
     * 마켓플레이스 전체 사용자 목록 조회
     *
     * @param httpServletRequest
     * @return
     */
    @GetMapping(value = "/users")
    @ResponseBody
    public CustomPage<Object> getUserStatList(HttpServletRequest httpServletRequest) {
        return adminStatsService.getUserList(commonService.setParameters(httpServletRequest));
    }


}
