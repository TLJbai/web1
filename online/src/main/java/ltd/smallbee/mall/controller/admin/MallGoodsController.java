package ltd.smallbee.mall.controller.admin;

import ltd.smallbee.mall.common.Constants;
import ltd.smallbee.mall.common.MallCategoryLevelEnum;
import ltd.smallbee.mall.common.ServiceResultEnum;
import ltd.smallbee.mall.entity.GoodsCategory;
import ltd.smallbee.mall.entity.MallGoods;
import ltd.smallbee.mall.service.MallGoodsCategoryService;
import ltd.smallbee.mall.service.MallGoodsService;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.Result;
import ltd.smallbee.mall.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// 后台管理系统中管理商品功能
@Controller
@RequestMapping("/admin")
public class MallGoodsController {
    @Resource
    private MallGoodsService mallGoodsService;
    @Resource
    private MallGoodsCategoryService mallGoodsCategoryService;

    /**
     * 获取页面
     *
     * @param request
     * @return
     */
    @GetMapping("/goods")
    public String getGoodsPage(HttpServletRequest request){
        request.setAttribute("path","newbee_mall_goods");
        return "admin/newbee_mall_goods";
    }

    /**
     * 列表
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/goods/list",method = RequestMethod.GET)
    @ResponseBody
    public Result list(@RequestParam Map<String,Object> params){
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))){
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil queryUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(mallGoodsService.getNewBeeMallGoodsPage(queryUtil));
    }

    /**
     * 默认编辑
     *
     * @param request
     * @return
     */
    @GetMapping("/goods/edit")
    public String edit(HttpServletRequest request){
        request.setAttribute("path","edit");
        //查询所有的一级分类
        List<GoodsCategory> firstLevelCategories = mallGoodsCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(0L), MallCategoryLevelEnum.LEVEL_ONE.getLevel());
        if (!CollectionUtils.isEmpty(firstLevelCategories)){
            //查询一级分类列表中第一个实体的所有二级分类
            List<GoodsCategory> secondLevelCategories = mallGoodsCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(firstLevelCategories.get(0).getCategoryId()), MallCategoryLevelEnum.LEVEL_TWO.getLevel());
            if (!CollectionUtils.isEmpty(secondLevelCategories)){
                //查询二级分类列表中第一个实体的所有三级分类
                List<GoodsCategory> thirdLevelCategories = mallGoodsCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(secondLevelCategories.get(0).getParentId()), MallCategoryLevelEnum.LEVEL_THREE.getLevel());
                request.setAttribute("firstLevelCategories", firstLevelCategories);
                request.setAttribute("secondLevelCategories", secondLevelCategories);
                request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                request.setAttribute("path", "goods-edit");
                return "admin/newbee_mall_goods_edit";
            }
        }
        return "error/error_5xx";
    }

    /**
     * 选择编辑
     *
     * @param request
     * @param goodsId
     * @return
     */
    @GetMapping("/goods/edit/{goodsId}")
    public String edit(HttpServletRequest request,@PathVariable("goodsId")Long goodsId){
        request.setAttribute("path","edit");
        MallGoods mallGoods = mallGoodsService.selectGoodsInfoById(goodsId);
        System.out.println("=======页面获取到的id：" + goodsId);
        if (mallGoods == null){
            return "error/error_5xx";
        }
        if (mallGoods.getGoodsCategoryId()>0){
            if (mallGoods.getGoodsCategoryId() != null || mallGoods.getGoodsCategoryId()>0) {
                //有分类字段则查询相关分类数据返回给前端以供分类的三级联动显示
                GoodsCategory currentGoodsCategory = mallGoodsCategoryService.getGoodsCategoryById(mallGoods.getGoodsCategoryId());
                //判断是否是三级分类
                if (currentGoodsCategory != null && currentGoodsCategory.getCategoryLevel() == MallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
                    //查询所有一级分类
                    List<GoodsCategory> firstLevelCategories = mallGoodsCategoryService.selectByLevelAndParentIdsAndNumber(
                            Collections.singletonList(0L), MallCategoryLevelEnum.LEVEL_ONE.getLevel());
                    //查询当前parentID下的所有三级分类
                    List<GoodsCategory> thirdLevelCategories = mallGoodsCategoryService
                            .selectByLevelAndParentIdsAndNumber(Collections.singletonList(currentGoodsCategory.getParentId()),
                                    MallCategoryLevelEnum.LEVEL_THREE.getLevel());
                    //查询查询当前三级分类的父级二级分类
                    GoodsCategory secondCategory = mallGoodsCategoryService.getGoodsCategoryById(currentGoodsCategory.getParentId());
                    if (secondCategory != null) {
                        //根据parentId查询当前parentId下所有的二级分类
                        List<GoodsCategory> secondLevelCategories = mallGoodsCategoryService.selectByLevelAndParentIdsAndNumber(
                                Collections.singletonList(secondCategory.getParentId()), MallCategoryLevelEnum.LEVEL_TWO.getLevel());
                        //
                        GoodsCategory firestCategory = mallGoodsCategoryService.getGoodsCategoryById(secondCategory.getParentId());
                        if (firestCategory != null) {
                            request.setAttribute("firstLevelCategories", firstLevelCategories);
                            request.setAttribute("secondLevelCategories", secondLevelCategories);
                            request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                            request.setAttribute("firstLevelCategoryId", firestCategory.getCategoryId());
                            request.setAttribute("secondLevelCategoryId", secondCategory.getCategoryId());
                            request.setAttribute("thirdLevelCategoryId", currentGoodsCategory.getCategoryId());
                        }
                    }
                }
            }
        }

        if (mallGoods.getGoodsCategoryId() == 0) {
            //查询所有的一级分类
            List<GoodsCategory> firstLevelCategories = mallGoodsCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(0L), MallCategoryLevelEnum.LEVEL_ONE.getLevel());
            if (!CollectionUtils.isEmpty(firstLevelCategories)) {
                //查询一级分类列表中第一个实体的所有二级分类
                List<GoodsCategory> secondLevelCategories = mallGoodsCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(firstLevelCategories.get(0).getCategoryId()), MallCategoryLevelEnum.LEVEL_TWO.getLevel());
                if (!CollectionUtils.isEmpty(secondLevelCategories)) {
                    //查询二级分类列表中第一个实体的所有三级分类
                    List<GoodsCategory> thirdLevelCategories = mallGoodsCategoryService.selectByLevelAndParentIdsAndNumber(Collections.singletonList(secondLevelCategories.get(0).getCategoryId()), MallCategoryLevelEnum.LEVEL_THREE.getLevel());
                    request.setAttribute("firstLevelCategories", firstLevelCategories);
                    request.setAttribute("secondLevelCategories", secondLevelCategories);
                    request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                }
            }
        }
        request.setAttribute("goods", mallGoods);
        request.setAttribute("path", "goods-edit");
        return "admin/newbee_mall_goods_edit";
        }

    /**
     *物品更新
     *
     * @param mallGoods
     * @return
     */
    @RequestMapping(value = "/goods/update",method = RequestMethod.POST)
        @ResponseBody
        public Result update(@RequestBody MallGoods mallGoods){
            if (Objects.isNull(mallGoods.getGoodsId())
                    || StringUtils.isEmpty(mallGoods.getGoodsName())
                    || StringUtils.isEmpty(mallGoods.getGoodsIntro())
                    || StringUtils.isEmpty(mallGoods.getTag())
                    || Objects.isNull(mallGoods.getOriginalPrice())
                    || Objects.isNull(mallGoods.getSellingPrice())
                    || Objects.isNull(mallGoods.getGoodsCategoryId())
                    || Objects.isNull(mallGoods.getStockNum())
                    || Objects.isNull(mallGoods.getGoodsSellStatus())
                    || StringUtils.isEmpty(mallGoods.getGoodsCoverImg())
                    || StringUtils.isEmpty(mallGoods.getGoodsDetailContent())) {
                return ResultGenerator.genFailResult("参数异常！");
            }
            String result = mallGoodsService.updateNewBeeMallGoods(mallGoods);
            if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
                return ResultGenerator.genSuccessResult();
            } else {
                return ResultGenerator.genFailResult(result);
            }
        }

    /***
     * 商品的上架和下架
     *
     * @param ids
     * @param sellStatus
     * @return
     */
    @RequestMapping(value = "/goods/status/{sellStatus}",method = RequestMethod.PUT)
    @ResponseBody
    public Result updateGoodsStatus(@RequestBody Long[] ids, @PathVariable("sellStatus")int sellStatus){
        if (ids.length<1){
            return ResultGenerator.genFailResult("参数异常");
        }
        if (sellStatus != Constants.SELL_STATUS_UP && sellStatus != Constants.SELL_STATUS_DOWN){
            return ResultGenerator.genFailResult("状态异常");
        }
        if (mallGoodsService.batchUpdateSellStatus(ids,sellStatus)){
            return ResultGenerator.genSuccessResult();
        }else {
            return ResultGenerator.genFailResult("修改失败");
        }
    }


    @RequestMapping(value = "/goods/save",method = RequestMethod.POST)
    @ResponseBody
    public Result insert(@RequestBody MallGoods mallGoods){
        if (StringUtils.isEmpty(mallGoods.getGoodsName())
                || StringUtils.isEmpty(mallGoods.getGoodsIntro())
                || StringUtils.isEmpty(mallGoods.getTag())
                || Objects.isNull(mallGoods.getOriginalPrice())
                || Objects.isNull(mallGoods.getGoodsCategoryId())
                || Objects.isNull(mallGoods.getSellingPrice())
                || Objects.isNull(mallGoods.getStockNum())
                || Objects.isNull(mallGoods.getGoodsSellStatus())
                || StringUtils.isEmpty(mallGoods.getGoodsCoverImg())
                || StringUtils.isEmpty(mallGoods.getGoodsDetailContent())) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = mallGoodsService.saveNewBeeMallGoods(mallGoods);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)){
            return ResultGenerator.genSuccessResult();
        }else{
            return ResultGenerator.genFailResult("添加失败");
        }
    }

}
