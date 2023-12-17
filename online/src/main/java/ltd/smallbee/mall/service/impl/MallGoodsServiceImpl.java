package ltd.smallbee.mall.service.impl;


import ltd.smallbee.mall.common.MallCategoryLevelEnum;
import ltd.smallbee.mall.common.ServiceResultEnum;
import ltd.smallbee.mall.controller.vo.NewBeeMallSearchGoodsVO;
import ltd.smallbee.mall.dao.GoodsCategoryMapper;
import ltd.smallbee.mall.dao.MallGoodsMapper;
import ltd.smallbee.mall.entity.GoodsCategory;
import ltd.smallbee.mall.entity.MallGoods;
import ltd.smallbee.mall.service.MallGoodsService;
import ltd.smallbee.mall.util.BeanUtil;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MallGoodsServiceImpl implements MallGoodsService {
    @Autowired
    private MallGoodsMapper mallGoodsMapper;
    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public MallGoods selectGoodsInfoById(Long id) {
        return mallGoodsMapper.selectGoodsInfoById(id);
    }

    @Override
    public PageResult getNewBeeMallGoodsPage(PageQueryUtil pageUtil) {
        List<MallGoods> goodsList = mallGoodsMapper.findNewBeeMallGoodsList(pageUtil);
        int total = mallGoodsMapper.getTotalNewBeeMallGoods(pageUtil);
        PageResult result = new PageResult(goodsList,total,pageUtil.getLimit(),pageUtil.getPage());
        return result;
    }

    @Override
    public String saveNewBeeMallGoods(MallGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != MallCategoryLevelEnum.LEVEL_THREE.getLevel()){
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        if (mallGoodsMapper.selectByCategoryIdAndName(goods.getGoodsName(),goodsCategory.getCategoryId()) != null){
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        if (mallGoodsMapper.insertSelective(goods) > 0){
            return ServiceResultEnum.SUCCESS.getResult();
        }else {
            return ServiceResultEnum.DB_ERROR.getResult();
        }

    }

    @Override
    public void batchSaveNewBeeMallGoods(List<MallGoods> mallGoodsList) {
        if ( !CollectionUtils.isEmpty(mallGoodsList)){
            mallGoodsMapper.batchInsert(mallGoodsList);
        }
    }

    @Override
    public String updateNewBeeMallGoods(MallGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != MallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        MallGoods temp = mallGoodsMapper.selectGoodsInfoById(goods.getGoodsId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        MallGoods temp2 = mallGoodsMapper.selectByCategoryIdAndName(goods.getGoodsName(), goods.getGoodsCategoryId());
        if (temp2 != null && !temp2.getGoodsId().equals(goods.getGoodsId())) {
            //name和分类id相同且不同id 不能继续修改
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        goods.setUpdateTime(new Date());
        if (mallGoodsMapper.updateByPrimaryKeySelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public Boolean batchUpdateSellStatus(Long[] ids, int sellStatus) {
        return mallGoodsMapper.batchUpdateSellStatus(ids,sellStatus) > 0;
    }

    @Override
    public PageResult searchNewBeeMallGoods(PageQueryUtil pageUtil) {
        List<MallGoods> goodsList = mallGoodsMapper.findNewBeeMallGoodsListBySearch(pageUtil);
        int total = mallGoodsMapper.getTotalNewBeeMallGoodsBySearch(pageUtil);
        List<NewBeeMallSearchGoodsVO> newBeeMallSearchGoodsVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            newBeeMallSearchGoodsVOS = BeanUtil.copyList(goodsList, NewBeeMallSearchGoodsVO.class);
            for (NewBeeMallSearchGoodsVO newBeeMallSearchGoodsVO : newBeeMallSearchGoodsVOS) {
                String goodsName = newBeeMallSearchGoodsVO.getGoodsName();
                String goodsIntro = newBeeMallSearchGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    newBeeMallSearchGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 30) {
                    goodsIntro = goodsIntro.substring(0, 30) + "...";
                    newBeeMallSearchGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        PageResult pageResult = new PageResult(newBeeMallSearchGoodsVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }
}
