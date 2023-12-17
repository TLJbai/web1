package ltd.smallbee.mall.controller.mall;

import ltd.smallbee.mall.common.Constants;
import ltd.smallbee.mall.common.MallException;
import ltd.smallbee.mall.common.ServiceResultEnum;
import ltd.smallbee.mall.controller.vo.NewBeeMallGoodsDetailVO;
import ltd.smallbee.mall.controller.vo.SearchPageCategoryVO;
import ltd.smallbee.mall.entity.MallGoods;
import ltd.smallbee.mall.service.MallGoodsCategoryService;
import ltd.smallbee.mall.service.MallGoodsService;
import ltd.smallbee.mall.util.BeanUtil;
import ltd.smallbee.mall.util.PageQueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class GoodsController {
    @Autowired
    private MallGoodsService mallGoodsService;
    @Autowired
    private MallGoodsCategoryService mallGoodsCategoryService;

    //详情页查找
    @GetMapping(value = "/goods/detail/{id}")
    public String goodInfo(@PathVariable("id")long id,
                           HttpServletRequest request,
                           HttpSession session){
        if (id<1){
            return "error_5xx";
        }
        //从数据库中查找
        MallGoods getGood = mallGoodsService.selectGoodsInfoById(id);
        //判定是否为空,如果为空，抛出设定好的异常
        if ( null == getGood)
            MallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        //同理，判定该商品是否下架
        if (Constants.SELL_STATUS_UP != getGood.getGoodsSellStatus())
            MallException.fail(ServiceResultEnum.GOODS_PUT_DOWN.getResult());
        //创建一个视图对象，方便再次查询和防止修改
        NewBeeMallGoodsDetailVO goodsDetailVO = new NewBeeMallGoodsDetailVO();
        //将getGood复制到goodsDetailVO
        BeanUtil.copyProperties(getGood,goodsDetailVO);
        goodsDetailVO.setGoodsCarouselList(getGood.getGoodsCarousel().split(","));

        System.out.println("获取的信息："+getGood.toString());
        request.setAttribute("goodsDetail",goodsDetailVO);
        return "mall/detail";
    }

    @GetMapping({"/search", "/search.html"})
    public String searchPage(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        if (StringUtils.isEmpty(params.get("page"))){
            params.put("page",1);
        }
        params.put("limit",Constants.GOODS_SEARCH_PAGE_LIMIT);
        //封装分类数据
        if (params.containsKey("goodsCategoryId") && !StringUtils.isEmpty(params.get("goodsCategoryId")+"")){
            Long goodsCategoryId = Long.valueOf(params.get("goodsCategoryId")+"");
            SearchPageCategoryVO searchPageCategoryVO = mallGoodsCategoryService.getCategoriesForSearch(goodsCategoryId);
            if (searchPageCategoryVO != null){
                request.setAttribute("goodsCategoryId",goodsCategoryId);
                request.setAttribute("searchPageCategoryVO",searchPageCategoryVO);
            }
        }
        //封装参数共前端显示
        if (params.containsKey("orderBy") && !StringUtils.isEmpty(params.get("orderBy")+"")){
            request.setAttribute("orderBy",params.get("orderBy")+"");
        }

        String keyword = "";
        if (params.containsKey("keyword") && !StringUtils.isEmpty((params.get("keyword") + "").trim())){
            keyword = params.get("keyword")+"";
        }
        request.setAttribute("keyword",keyword);
        params.put("keyword",keyword);
        //搜索上架状态下的商品
        params.put("goodsSellStatus", Constants.SELL_STATUS_UP);
        //封装商品数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        request.setAttribute("pageResult", mallGoodsService.searchNewBeeMallGoods(pageUtil));
        return "mall/search";
    }

}
