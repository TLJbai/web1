package ltd.smallbee.mall.service;

import ltd.smallbee.mall.controller.vo.NewBeeMallIndexConfigGoodsVO;
import ltd.smallbee.mall.entity.IndexConfig;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.PageResult;

import java.util.List;

public interface MallIndexConfigService {
    /**
     * 后台操作
     * @param pageQueryUtil
     * @return
     */
    //分页查找
    PageResult getConfigPage(PageQueryUtil pageQueryUtil);
    //批量删除
    Boolean deleteBatch(Long[] ids);
    //通过id查询
    IndexConfig getIndexConfigById(Long id);
    //更新
    String updateIndexConfig(IndexConfig indexConfig);
    //保存
    String saveIndexConfig(IndexConfig indexConfig);

    /**
     * 前台操作-同步显示
     * @param configType
     * @param number
     * @return
     */
    List<NewBeeMallIndexConfigGoodsVO> getConfigGoodsesForIndex(int configType, int number);

}
