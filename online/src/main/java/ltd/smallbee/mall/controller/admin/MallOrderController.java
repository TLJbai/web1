package ltd.smallbee.mall.controller.admin;

import ltd.smallbee.mall.common.ServiceResultEnum;
import ltd.smallbee.mall.controller.vo.NewBeeMallOrderItemVO;
import ltd.smallbee.mall.entity.MallOrder;
import ltd.smallbee.mall.service.MallOrderService;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.Result;
import ltd.smallbee.mall.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// 后台管理系统中管理订单功能
@Controller
@RequestMapping("/admin")
public class MallOrderController {
    @Resource
    MallOrderService mallOrderService;

    @GetMapping("/orders")
    public String ordersPage(HttpServletRequest request) {
        request.setAttribute("path", "orders");
        return "admin/newbee_mall_order";
    }

    /***
     * 列表
     *
     * @param params
     * @return
     */
    @RequestMapping("/orders/list")
    @ResponseBody
    public Result list (@RequestParam Map<String,Object> params){
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))){
            return ResultGenerator.genFailResult("参数异常");
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(mallOrderService.getNewBeeMallOrdersPage(pageUtil));
    }


    /**
     * 修改
     *
     * @param mallOrder
     * @return
     */
    @RequestMapping(value = "/orders/update", method = RequestMethod.POST)
    @ResponseBody
    public Result update(@RequestBody MallOrder mallOrder) {
        if (Objects.isNull(mallOrder.getTotalPrice())
                || Objects.isNull(mallOrder.getOrderId())
                || mallOrder.getOrderId() < 1
                || mallOrder.getTotalPrice() < 1
                || StringUtils.isEmpty(mallOrder.getUserAddress())) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = mallOrderService.updateOrderInfo(mallOrder);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /***
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/order-items/{id}")
    @ResponseBody
    public Result info(@PathVariable("id") Long id) {
        List<NewBeeMallOrderItemVO> orderItems = mallOrderService.getOrderItems(id);
        if (!CollectionUtils.isEmpty(orderItems)) {
            return ResultGenerator.genSuccessResult(orderItems);
        }
        return ResultGenerator.genFailResult(ServiceResultEnum.DATA_NOT_EXIST.getResult());
    }

    /***
     * 配货
     * @param ids
     * @return
     */

    @RequestMapping(value = "/orders/checkDone", method = RequestMethod.POST)
    @ResponseBody
    public Result checkDone(@RequestBody Long[] ids){
        if (ids.length <1 ){
            return ResultGenerator.genFailResult("参数异常");
        }
        String result = mallOrderService.checkDone(ids);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 出库
     */
    @RequestMapping(value = "/orders/checkOut", method = RequestMethod.POST)
    @ResponseBody
    public Result checkOut(@RequestBody Long[] ids) {
        if (ids.length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = mallOrderService.checkOut(ids);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 关闭订单
     */
    @RequestMapping(value = "/orders/close", method = RequestMethod.POST)
    @ResponseBody
    public Result closeOrder(@RequestBody Long[] ids) {
        if (ids.length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = mallOrderService.closeOrder(ids);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }



}
