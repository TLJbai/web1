package ltd.smallbee.mall.controller.admin;

import ltd.smallbee.mall.common.IndexConfigTypeEnum;
import ltd.smallbee.mall.common.ServiceResultEnum;
import ltd.smallbee.mall.entity.IndexConfig;
import ltd.smallbee.mall.service.MallIndexConfigService;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.Result;
import ltd.smallbee.mall.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

// 后台管理系统中管理首页配置（除了轮播图）
@Controller
@RequestMapping("/admin")
public class MallIndexConfigController {
    @Resource
    private MallIndexConfigService mallIndexConfigService;

    /***
     * 按类型显示相关页面： 3-(首页)热销商品 4-(首页)新品上线 5-(首页)为你推荐
     * @param request
     * @param configType
     * @return
     */
    @GetMapping("/indexConfigs")
    public String indexConfigsPage(HttpServletRequest request, @RequestParam("configType") int configType){
        IndexConfigTypeEnum indexConfigTypeEnum = IndexConfigTypeEnum.getIndexConfigTypeEnumByType(configType);
        if (indexConfigTypeEnum.equals(IndexConfigTypeEnum.DEFAULT)){
            return "error/error.5xx";
        }
        request.setAttribute("path",indexConfigTypeEnum.getName());
        request.setAttribute("configType",configType);
        return "admin/newbee_mall_index_config";
    }

    /**
     * 分页查找
     * @param params
     * @return
     */
    @RequestMapping(value = "/indexConfigs/list",method = RequestMethod.GET)
    @ResponseBody
    public Result list(@RequestParam Map<String,Object> params){
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))){
            return ResultGenerator.genFailResult("参数异常");
        }

        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(mallIndexConfigService.getConfigPage(pageUtil));
    }

    @RequestMapping(value = "/indexConfigs/delete",method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@RequestBody Long[] ids){
        if (ids.length<1){
            return ResultGenerator.genFailResult("参数异常!");
        }
        if (mallIndexConfigService.deleteBatch(ids)){
            return ResultGenerator.genSuccessResult();
        }else {
            return ResultGenerator.genFailResult("删除失败");
        }
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @GetMapping("/indexConfigs/info/{id}")
    @ResponseBody
    public Result info(@PathVariable("id")Long id){
        IndexConfig result = mallIndexConfigService.getIndexConfigById(id);
        if (null == result){
            return ResultGenerator.genFailResult("为查询到数据");
        }
        return ResultGenerator.genSuccessResult(result);
    }

    /**
     * 添加
     */
    @RequestMapping(value = "/indexConfigs/save",method = RequestMethod.POST)
    @ResponseBody
    public Result save(@RequestBody IndexConfig indexConfig){
        if (Objects.isNull(indexConfig.getConfigType())
                || StringUtils.isEmpty(indexConfig.getConfigName())
                || Objects.isNull(indexConfig.getConfigRank())){
            return ResultGenerator.genFailResult("参数异常!");
        }
        String result = mallIndexConfigService.saveIndexConfig(indexConfig);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)){
            return ResultGenerator.genSuccessResult();
        }else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 修改
     * @param indexConfig
     * @return
     */
    @RequestMapping(value = "/indexConfigs/update",method = RequestMethod.POST)
    @ResponseBody
    public Result update(@RequestBody IndexConfig indexConfig){
        if (Objects.isNull(indexConfig.getConfigType())
                || Objects.isNull(indexConfig.getConfigId())
                || Objects.isNull(indexConfig.getConfigRank())
                || StringUtils.isEmpty(indexConfig.getConfigName())){
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = mallIndexConfigService.updateIndexConfig(indexConfig);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)){
            return ResultGenerator.genSuccessResult();
        }else {
            return ResultGenerator.genFailResult(result);
        }
    }
}
