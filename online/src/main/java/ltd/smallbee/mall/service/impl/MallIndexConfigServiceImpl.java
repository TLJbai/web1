package ltd.smallbee.mall.service.impl;

import ltd.smallbee.mall.common.ServiceResultEnum;
import ltd.smallbee.mall.controller.vo.NewBeeMallIndexConfigGoodsVO;
import ltd.smallbee.mall.dao.IndexConfigMapper;
import ltd.smallbee.mall.dao.MallGoodsMapper;
import ltd.smallbee.mall.entity.IndexConfig;
import ltd.smallbee.mall.entity.MallGoods;
import ltd.smallbee.mall.service.MallIndexConfigService;
import ltd.smallbee.mall.util.BeanUtil;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallIndexConfigServiceImpl implements MallIndexConfigService {
    @Autowired
    private IndexConfigMapper indexConfigMapper;
    @Autowired
    private MallGoodsMapper goodsMapper;

    @Override
    public PageResult getConfigPage(PageQueryUtil pageUtil) {
        List<IndexConfig> indexConfigList = indexConfigMapper.findIndexConfigList(pageUtil);
        int total = indexConfigMapper.getTotalIndexConfig(pageUtil);
        PageResult pageResult = new PageResult(indexConfigList,total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public Boolean deleteBatch(Long[] ids) {
        if (ids.length <1){
            return false;
        }
        return indexConfigMapper.deleteBatch(ids)>0;
    }

    @Override
    public IndexConfig getIndexConfigById(Long id) {
        return indexConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    public String updateIndexConfig(IndexConfig indexConfig) {
        IndexConfig temp = indexConfigMapper.selectByPrimaryKey(indexConfig.getConfigId());
        if (temp == null){
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
//        IndexConfig temp2 = indexConfigMapper.selectByTypeAndGoodsId(indexConfig.getConfigType(), indexConfig.getGoodsId());
//        if (temp2 != null && !temp2.getConfigId().equals(indexConfig.getConfigId())){
//            //goodsId相同且不同id 不能继续修改
//            return ServiceResultEnum.SAME_INDEX_CONFIG_EXIST.getResult();
//        }
        indexConfig.setUpdateTime(new Date());
        if (indexConfigMapper.updateByPrimaryKeySelective(indexConfig)>0){
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String saveIndexConfig(IndexConfig indexConfig) {
        if (indexConfigMapper.insertSelective(indexConfig) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public List<NewBeeMallIndexConfigGoodsVO> getConfigGoodsesForIndex(int configType, int number) {
        List<NewBeeMallIndexConfigGoodsVO> newBeeMallIndexConfigGoodsVOS = new ArrayList<>(number);
        List<IndexConfig> indexConfigs = indexConfigMapper.findIndexConfigsByTypeAndNum(configType, number);
        if (!CollectionUtils.isEmpty(indexConfigs)) {
            //取出所有的goodsId
            List<Long> goodsIds = indexConfigs.stream().map(IndexConfig::getGoodsId).collect(Collectors.toList());
            List<MallGoods> mallGoods = goodsMapper.selectByPrimaryKeys(goodsIds);
            newBeeMallIndexConfigGoodsVOS = BeanUtil.copyList(mallGoods, NewBeeMallIndexConfigGoodsVO.class);
            for (NewBeeMallIndexConfigGoodsVO newBeeMallIndexConfigGoodsVO : newBeeMallIndexConfigGoodsVOS) {
                String goodsName = newBeeMallIndexConfigGoodsVO.getGoodsName();
                String goodsIntro = newBeeMallIndexConfigGoodsVO.getGoodsIntro();
                // 解决字符串过长导致文字超出的问题
                if (goodsName.length() > 30) {
                    goodsName = goodsName.substring(0, 30) + "...";
                    newBeeMallIndexConfigGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 22) {
                    goodsIntro = goodsIntro.substring(0, 22) + "...";
                    newBeeMallIndexConfigGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        return newBeeMallIndexConfigGoodsVOS;
    }
}
