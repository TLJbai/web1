/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.smallbee.mall.service.impl;


import ltd.smallbee.mall.common.*;
import ltd.smallbee.mall.dao.MallGoodsMapper;
import ltd.smallbee.mall.dao.MallOrderItemMapper;
import ltd.smallbee.mall.dao.MallOrderMapper;
import ltd.smallbee.mall.dao.MallShoppingCartItemMapper;
import ltd.smallbee.mall.service.MallOrderService;
import ltd.smallbee.mall.util.BeanUtil;
import ltd.smallbee.mall.util.NumberUtil;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.PageResult;
import ltd.smallbee.mall.entity.MallGoods;
import ltd.smallbee.mall.entity.MallOrder;
import ltd.smallbee.mall.entity.MallOrderItem;
import ltd.smallbee.mall.entity.StockNumDTO;
import ltd.smallbee.mall.controller.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
public class MallOrderServiceImpl implements MallOrderService {
    @Autowired
    public MallOrderMapper mallOrderMapper;
    @Autowired
    public MallOrderItemMapper mallOrderItemMapper;
    @Autowired
    public MallGoodsMapper mallGoodsMapper;
    @Autowired
    public MallShoppingCartItemMapper mallShoppingCartItemMapper;

    /***
     * 展示列表
     *
     * @param pageUtil
     * @return
     */
    @Override
    public PageResult getNewBeeMallOrdersPage(PageQueryUtil pageUtil) {
        List<MallOrder> mallOrderList = mallOrderMapper.findNewBeeMallOrderList(pageUtil);
        int total = mallOrderMapper.getTotalNewBeeMallOrders(pageUtil);
        PageResult result = new PageResult(mallOrderList,total,pageUtil.getLimit(), pageUtil.getPage());
        return result;
    }

    /***
     * 修改订单
     *
     * @param mallOrder
     * @return
     */
    @Override
    public String updateOrderInfo(MallOrder mallOrder) {
        MallOrder temp = mallOrderMapper.selectByPrimaryKey(mallOrder.getOrderId());
        if (temp != null && temp.getOrderStatus() >=0 && temp.getOrderStatus() <3){
            temp.setTotalPrice(mallOrder.getTotalPrice());
            temp.setUserAddress(mallOrder.getUserAddress());
            temp.setUpdateTime(new Date());
            if (mallOrderMapper.updateByPrimaryKeySelective(temp) > 0){
                return ServiceResultEnum.SUCCESS.getResult();
            }
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }


    /***
     * 对已支付的订单进行配货
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public String checkDone(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<MallOrder> orders  = mallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorRecordNos = "";
        if (!CollectionUtils.isEmpty(orders)){
            for (MallOrder order : orders){
                if (order.getIsDeleted() == 1){
                    errorRecordNos += order.getOrderNo() + " ";
                    continue;
                }
                if (order.getOrderStatus() != 1){
                    errorRecordNos += order.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorRecordNos)){
                //所有选择的订单状态正常 可以执行配货完成操作 修改订单状态和更新时间
                if (mallOrderMapper.checkDone(Arrays.asList(ids)) > 0){
                    return ServiceResultEnum.SUCCESS.getResult();
                }else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            }else {
                //订单此时不可执行出库操作
                if (errorRecordNos.length()>0 && errorRecordNos.length()<100){
                    return errorRecordNos + "订单的状态不是支付成功无法执行出库操作";
                }else{
                    return "你选择了太多状态不是支付成功的订单，无法执行配货完成操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    /***
     * 出货
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public String checkOut(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<MallOrder> orders = mallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (MallOrder mallOrder : orders) {
                if (mallOrder.getIsDeleted() == 1) {
                    errorOrderNos += mallOrder.getOrderNo() + " ";
                    continue;
                }
                if (mallOrder.getOrderStatus() != 1 && mallOrder.getOrderStatus() != 2) {
                    errorOrderNos += mallOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //订单状态正常 可以执行出库操作 修改订单状态和更新时间
                if (mallOrderMapper.checkOut(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    /**
     * 手动关闭
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<MallOrder> orders = mallOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (MallOrder mallOrder : orders) {
                // isDeleted=1 一定为已关闭订单
                if (mallOrder.getIsDeleted() == 1) {
                    errorOrderNos += mallOrder.getOrderNo() + " ";
                    continue;
                }
                //已关闭或者已完成无法关闭订单
                if (mallOrder.getOrderStatus() == 4 || mallOrder.getOrderStatus() < 0) {
                    errorOrderNos += mallOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //订单状态正常 可以执行关闭操作 修改订单状态和更新时间
                if (mallOrderMapper.closeOrder(Arrays.asList(ids), MallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行关闭操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    /***
     * 保存订单
     *
     * @param user
     * @param myShoppingCartItems
     * @return
     */
    @Override
    @Transactional
    public String saveOrder(NewBeeMallUserVO user, List<NewBeeMallShoppingCartItemVO> myShoppingCartItems) {
        List<Long> itemIdList = myShoppingCartItems.stream().map(NewBeeMallShoppingCartItemVO::getCartItemId).collect(Collectors.toList());
        List<Long> goodsIds = myShoppingCartItems.stream().map(NewBeeMallShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
        List<MallGoods> mallGoods = mallGoodsMapper.selectByPrimaryKeys(goodsIds);
        //检查是否包含已下架商品
        List<MallGoods> goodsListNotSelling = mallGoods.stream()
                .filter(newBeeMallGoodsTemp -> newBeeMallGoodsTemp.getGoodsSellStatus() != Constants.SELL_STATUS_UP)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(goodsListNotSelling)) {
            //goodsListNotSelling 对象非空则表示有下架商品
            MallException.fail(goodsListNotSelling.get(0).getGoodsName() + "已下架，无法生成订单");
        }
        Map<Long, MallGoods> newBeeMallGoodsMap = mallGoods.stream().collect(Collectors.toMap(MallGoods::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
        //判断商品库存
        for (NewBeeMallShoppingCartItemVO shoppingCartItemVO : myShoppingCartItems) {
            //查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!newBeeMallGoodsMap.containsKey(shoppingCartItemVO.getGoodsId())) {
                MallException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
            }
            //存在数量大于库存的情况，直接返回错误提醒
            if (shoppingCartItemVO.getGoodsCount() > newBeeMallGoodsMap.get(shoppingCartItemVO.getGoodsId()).getStockNum()) {
                MallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
            }
        }
        //删除购物项
        if (!CollectionUtils.isEmpty(itemIdList) && !CollectionUtils.isEmpty(goodsIds) && !CollectionUtils.isEmpty(mallGoods)) {
            if (mallShoppingCartItemMapper.deleteBatch(itemIdList) > 0) {
                List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(myShoppingCartItems, StockNumDTO.class);
                int updateStockNumResult = mallGoodsMapper.updateStockNum(stockNumDTOS);
                if (updateStockNumResult < 1) {
                    MallException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
                }
                //生成订单号
                String orderNo = NumberUtil.genOrderNo();
                int priceTotal = 0;
                //保存订单
                MallOrder mallOrder = new MallOrder();
                mallOrder.setOrderNo(orderNo);
                mallOrder.setUserId(user.getUserId());
                mallOrder.setUserAddress(user.getAddress());
                //总价
                for (NewBeeMallShoppingCartItemVO newBeeMallShoppingCartItemVO : myShoppingCartItems) {
                    priceTotal += newBeeMallShoppingCartItemVO.getGoodsCount() * newBeeMallShoppingCartItemVO.getSellingPrice();
                }
                if (priceTotal < 1) {
                    MallException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
                }
                mallOrder.setTotalPrice(priceTotal);
                //订单body字段，用来作为生成支付单描述信息，暂时未接入第三方支付接口，故该字段暂时设为空字符串
                String extraInfo = "";
                mallOrder.setExtraInfo(extraInfo);
                //生成订单项并保存订单项纪录
                if (mallOrderMapper.insertSelective(mallOrder) > 0) {
                    //生成所有的订单项快照，并保存至数据库
                    List<MallOrderItem> mallOrderItems = new ArrayList<>();
                    for (NewBeeMallShoppingCartItemVO newBeeMallShoppingCartItemVO : myShoppingCartItems) {
                        MallOrderItem mallOrderItem = new MallOrderItem();
                        //使用BeanUtil工具类将newBeeMallShoppingCartItemVO中的属性复制到newBeeMallOrderItem对象中
                        BeanUtil.copyProperties(newBeeMallShoppingCartItemVO, mallOrderItem);
                        //NewBeeMallOrderMapper文件insert()方法中使用了useGeneratedKeys因此orderId可以获取到
                        mallOrderItem.setOrderId(mallOrder.getOrderId());
                        mallOrderItems.add(mallOrderItem);
                    }
                    //保存至数据库
                    if (mallOrderItemMapper.insertBatch(mallOrderItems) > 0) {
                        //所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
                        return orderNo;
                    }
                    MallException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
                }
                MallException.fail(ServiceResultEnum.DB_ERROR.getResult());
            }
            MallException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        MallException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
        return ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult();
    }

    /***
     * 获得订单详情
     *
     * @param orderNo
     * @param userId
     * @return
     */
    @Override
    public NewBeeMallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        MallOrder mallOrder = mallOrderMapper.selectByOrderNo(orderNo);
        if (mallOrder != null){
            //验证是否是当前userId下的订单，否则报错
            if ( !userId.equals(mallOrder.getUserId())){
                MallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
            List<MallOrderItem> orderItems = mallOrderItemMapper.selectByOrderId(mallOrder.getOrderId());
            //获取订单项数据
            if (!CollectionUtils.isEmpty(orderItems)){
                List<NewBeeMallOrderItemVO> itemVOS = BeanUtil.copyList(orderItems,NewBeeMallOrderItemVO.class);
                NewBeeMallOrderDetailVO newBeeMallOrderDetailVO = new NewBeeMallOrderDetailVO();
                BeanUtil.copyProperties(mallOrder,newBeeMallOrderDetailVO);
                newBeeMallOrderDetailVO.setNewBeeMallOrderItemVOS(itemVOS);
                newBeeMallOrderDetailVO.setOrderStatusString(MallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(newBeeMallOrderDetailVO.getOrderStatus()).getName());
                newBeeMallOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(mallOrder.getPayType()).getName());
                return newBeeMallOrderDetailVO;
            }
        }
        return null;
    }

    /***
     * 获得订单
     *
     * @param orderNo
     * @return
     */
    @Override
    public MallOrder getNewBeeMallOrderByOrderNo(String orderNo) {
        return mallOrderMapper.selectByOrderNo(orderNo);
    }

    /**
     * 获取个人订单
     *
     * @param pageUtil
     * @return
     */
    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        int total = mallOrderMapper.getTotalNewBeeMallOrders(pageUtil);
        //找出所有订单
        List<MallOrder> mallOrders = mallOrderMapper.findNewBeeMallOrderList(pageUtil);
        //创建空的视图订单表
        List<NewBeeMallOrderListVO> orderListVOS = new ArrayList<>();
        if (total > 0 ){
            //数据转换，将实体类转成vo
            orderListVOS = BeanUtil.copyList(mallOrders,NewBeeMallOrderListVO.class);
            //设置订单状态中文显示值
            for (NewBeeMallOrderListVO newBeeMallOrderListVO : orderListVOS){
                newBeeMallOrderListVO.setOrderStatusString(MallOrderStatusEnum.getNewBeeMallOrderStatusEnumByStatus(newBeeMallOrderListVO.getOrderStatus()).getName());
            }
            //将所有订单id放入list中
            List<Long> orderIds = mallOrders.stream().map(MallOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)){
                //创建一个list存入每个订单id对应的商品订单
                List<MallOrderItem> orderItems = mallOrderItemMapper.selectByOrderIds(orderIds);
                //创建一个由订单id分组的map
                Map<Long,List<MallOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(MallOrderItem::getOrderId));
                for (NewBeeMallOrderListVO newBeeMallOrderListVO : orderListVOS){
                    //封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(newBeeMallOrderListVO.getOrderId())){ //判断是否包含
                        List<MallOrderItem> orderItemTemp = itemByOrderIdMap.get(newBeeMallOrderListVO.getOrderId());
                        //将NewBeeMallOrderItem对象列表转换成NewBeeMallOrderItemVO对象列表
                        List<NewBeeMallOrderItemVO> orderItemVOList = BeanUtil.copyList(orderItemTemp,NewBeeMallOrderItemVO.class);
                        newBeeMallOrderListVO.setNewBeeMallOrderItemVOS(orderItemVOList);
                    }
                }
            }
        }
        PageResult pageResult =new PageResult(orderListVOS,total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }


    @Override
    public String cancelOrder(String orderNo, Long userId) {
        MallOrder mallOrder = mallOrderMapper.selectByOrderNo(orderNo);
        if (mallOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (!userId.equals(mallOrder.getUserId())) {
                MallException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
            //订单状态判断
            if (mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus()
                    || mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()
                    || mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus()
                    || mallOrder.getOrderStatus().intValue() == MallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            if (mallOrderMapper.closeOrder(Collections.singletonList(mallOrder.getOrderId()), MallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        MallOrder mallOrder = mallOrderMapper.selectByOrderNo(orderNo);
        if (mallOrder !=null){
            //验证是否是当前userId下的订单，否则报错
            if (! userId.equals(mallOrder.getUserId())){
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
            //订单状态判断 非出库状态下不进行修改操作
            if (mallOrder.getOrderStatus() != MallOrderStatusEnum.OREDER_EXPRESS.getOrderStatus()){
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            mallOrder.setOrderStatus((byte) MallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            mallOrder.setUpdateTime(new Date());
            if (mallOrderMapper.updateByPrimaryKeySelective(mallOrder) > 0){
                return ServiceResultEnum.SUCCESS.getResult();
            }else{
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        MallOrder mallOrder = mallOrderMapper.selectByOrderNo(orderNo);
        if (mallOrder != null) {
            //订单状态判断 非待支付状态下不进行修改操作
            if (mallOrder.getOrderStatus().intValue() != MallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            mallOrder.setOrderStatus((byte) MallOrderStatusEnum.OREDER_PAID.getOrderStatus());
            mallOrder.setPayType((byte) payType);
            mallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            mallOrder.setPayTime(new Date());
            mallOrder.setUpdateTime(new Date());
            if (mallOrderMapper.updateByPrimaryKeySelective(mallOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public List<NewBeeMallOrderItemVO> getOrderItems(Long id) {
        MallOrder mallOrder = mallOrderMapper.selectByPrimaryKey(id);
        if (mallOrder != null){
            List<MallOrderItem> itemList = mallOrderItemMapper.selectByOrderId(mallOrder.getOrderId());
            if ( !CollectionUtils.isEmpty(itemList)){
                List<NewBeeMallOrderItemVO> newBeeMallOrderItemVOS = BeanUtil.copyList(itemList,NewBeeMallOrderItemVO.class);
                return newBeeMallOrderItemVOS;
            }
        }
        return null;
    }
}