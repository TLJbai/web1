package ltd.smallbee.mall.controller.admin;

import ltd.smallbee.mall.common.ServiceResultEnum;
import ltd.smallbee.mall.entity.Carousel;
import ltd.smallbee.mall.service.MallCarouselService;
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

// 后台管理系统中管理轮播图
@Controller
@RequestMapping("/admin")
public class MallCarouselController {

    @Resource
    MallCarouselService mallCarouselService;
    //显示页面
    @GetMapping("/carousels")
    public String toCarousels(HttpServletRequest request){
        request.setAttribute("path","NewBee_Carousel");
        return "admin/newbee_mall_carousel";
    }

    //展示列表
    @RequestMapping(value = "/carousels/list", method = RequestMethod.GET)
    @ResponseBody
    public Result getCarouselsList(@RequestParam Map<String,Object> params ){
        if (StringUtils.isEmpty(params.get("page"))||StringUtils.isEmpty(params.get("limit"))){
            return ResultGenerator.genFailResult("参数异常");
        }
//        System.out.println("=============================================");
//        System.out.println("params -["+ params +"]-" );
        PageQueryUtil pageQueryUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(mallCarouselService.getCarouselByPage(pageQueryUtil));
    }

    //批量删除
    @RequestMapping(value = "/carousels/delete", method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@RequestBody Integer[] ids) {
        if (ids.length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if (mallCarouselService.deleteListById(ids)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("删除失败");
        }
    }

    //选定一个轮播图
    @RequestMapping(value = "/carousels/info/{id}")
    @ResponseBody
    public Result getOne(@PathVariable("id")Integer id){
        Carousel carousel = mallCarouselService.selectOneById(id);
        if (null == carousel){
            return ResultGenerator.genFailResult(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        //返回获取到的数据
        return ResultGenerator.genSuccessResult(carousel);
    }

    //修改选中的轮播图
    @RequestMapping(value = "/carousels/update", method = RequestMethod.POST)
    @ResponseBody
    public Result update(@RequestBody Carousel carousel){
        if (Objects.isNull(carousel.getCarouselId())
                || StringUtils.isEmpty(carousel.getCarouselUrl())
                || Objects.isNull(carousel.getCarouselRank())){
            return ResultGenerator.genFailResult("参数异常");
        }
        String result = mallCarouselService.updateCarousel(carousel);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)){
            return ResultGenerator.genSuccessResult();
        }else{
            return ResultGenerator.genFailResult(result);
        }
    }

    //新增轮播图
    @RequestMapping(value = "/carousels/save", method = RequestMethod.POST)
    @ResponseBody
    public Result save(@RequestBody Carousel carousel) {
        if (StringUtils.isEmpty(carousel.getCarouselUrl())
                || Objects.isNull(carousel.getCarouselRank())) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = mallCarouselService.saveCarousel(carousel);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }
}
