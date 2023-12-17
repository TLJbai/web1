package ltd.smallbee.mall.controller.admin;

import ltd.smallbee.mall.common.MallCategoryLevelEnum;
import ltd.smallbee.mall.common.ServiceResultEnum;
import ltd.smallbee.mall.entity.GoodsCategory;
import ltd.smallbee.mall.service.MallGoodsCategoryService;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.Result;
import ltd.smallbee.mall.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

// 后台管理系统中管理商品模块
@Controller
@RequestMapping("/admin")
public class MallGoodsCategoryController {

    @Resource
    private MallGoodsCategoryService goodsCategoryService;

    @GetMapping("/categories")
    public String categoriesPage(HttpServletRequest request,
                                 @RequestParam("categoryLevel") Byte categoryLevel,
                                 @RequestParam("parentId") Long parentId,
                                 @RequestParam("backParentId") Long backParentId){
        if (categoryLevel ==null || categoryLevel <1 || categoryLevel>3){
            return "error/error_5xx";
        }
        request.setAttribute("path", "newbee_mall_category");
        request.setAttribute("parentId", parentId);
        request.setAttribute("backParentId", backParentId);
        request.setAttribute("categoryLevel", categoryLevel);
        return "admin/newbee_mall_category";
    }

    /**
     * 分页
     * @param params
     * @return
     */
    @RequestMapping(value = "/categories/list" ,method = RequestMethod.GET)
    @ResponseBody
    public Result list(@RequestParam Map<String, Object> params){
        if (StringUtils.isEmpty(params.get("limit"))
                || StringUtils.isEmpty(params.get("page"))
                || StringUtils.isEmpty(params.get("categoryLevel"))
                || StringUtils.isEmpty(params.get("parentId"))){
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil queryUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(goodsCategoryService.getCategorisPage(queryUtil));
    }

    /**
     * 列表
     */
    @RequestMapping(value = "/categories/listForSelect", method = RequestMethod.GET)
    @ResponseBody
    public Result listForSelect(@RequestParam("categoryId") Long categoryId) {
        if (categoryId == null || categoryId < 1) {
            return ResultGenerator.genFailResult("缺少参数！");
        }
        GoodsCategory category = goodsCategoryService.getGoodsCategoryById(categoryId);
        //既不是一级分类也不是二级分类则为不返回数据
        if (category == null || category.getCategoryLevel() == MallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        Map categoryResult = new HashMap(4);
        if (category.getCategoryLevel() == MallCategoryLevelEnum.LEVEL_ONE.getLevel()) {
            //如果是一级分类则返回当前一级分类下的所有二级分类，以及二级分类列表中第一条数据下的所有三级分类列表
            //查询一级分类列表中第一个实体的所有二级分类
            List<GoodsCategory> secondLevelCategories = goodsCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(categoryId), MallCategoryLevelEnum.LEVEL_TWO.getLevel());
            if (!CollectionUtils.isEmpty(secondLevelCategories)) {
                //查询二级分类列表中第一个实体的所有三级分类
                List<GoodsCategory> thirdLevelCategories = goodsCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(secondLevelCategories.get(0).getCategoryId()), MallCategoryLevelEnum.LEVEL_THREE.getLevel());
                categoryResult.put("secondLevelCategories", secondLevelCategories);
                categoryResult.put("thirdLevelCategories", thirdLevelCategories);
            }
        }
        if (category.getCategoryLevel() == MallCategoryLevelEnum.LEVEL_TWO.getLevel()) {
            //如果是二级分类则返回当前分类下的所有三级分类列表
            List<GoodsCategory> thirdLevelCategories = goodsCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(categoryId), MallCategoryLevelEnum.LEVEL_THREE.getLevel());
            categoryResult.put("thirdLevelCategories", thirdLevelCategories);
        }
        return ResultGenerator.genSuccessResult(categoryResult);
    }

    /**
     * 添加
     * @param goodsCategory
     * @return
     */
    @RequestMapping(value = "/categories/save",method = RequestMethod.POST)
    @ResponseBody
    public Result save(@RequestBody GoodsCategory goodsCategory){
        if (Objects.isNull(goodsCategory.getCategoryLevel())
                || Objects.isNull(goodsCategory.getCategoryRank())
                || Objects.isNull(goodsCategory.getParentId())
                || StringUtils.isEmpty(goodsCategory.getCategoryName())){
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = goodsCategoryService.saveCategory(goodsCategory);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)){
            return ResultGenerator.genSuccessResult();
        }else {
            return ResultGenerator.genFailResult(result);
        }

    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @RequestMapping(value = "/categories/delete", method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@RequestBody Integer[] ids){
        if (goodsCategoryService.deleteBatch(ids)){
            return ResultGenerator.genSuccessResult();
        }else {
            return ResultGenerator.genFailResult("删除失败！");
        }
    }

    /**
     * 详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/categories/info/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Result info(@PathVariable("id")Long id){
        GoodsCategory goodsCategory = goodsCategoryService.getGoodsCategoryById(id);
        System.out.println("==========================");
        System.out.println("得到的数据： " + goodsCategory);
        if (goodsCategory == null){
            return ResultGenerator.genFailResult("未查询到数据");
        }
        return ResultGenerator.genSuccessResult(goodsCategory);
    }

    @RequestMapping(value = "/categories/update", method = RequestMethod.POST)
    @ResponseBody
    public Result update(@RequestBody GoodsCategory  goodsCategory){
        if (Objects.isNull(goodsCategory.getCategoryLevel())
                || Objects.isNull(goodsCategory.getCategoryRank())
                || Objects.isNull(goodsCategory.getParentId())
                || StringUtils.isEmpty(goodsCategory.getCategoryName())){
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = goodsCategoryService.updateGoodsCategory(goodsCategory);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)){
            return ResultGenerator.genSuccessResult();
        }else {
            return ResultGenerator.genFailResult(result);
        }
    }

}
