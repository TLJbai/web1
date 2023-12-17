package ltd.smallbee.mall.dao;

import ltd.smallbee.mall.entity.IndexConfig;
import ltd.smallbee.mall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IndexConfigMapper {
    //分页查找
    List<IndexConfig> findIndexConfigList(PageQueryUtil pageQueryUtil);
    //总记数量
    int getTotalIndexConfig(PageQueryUtil pageQueryUtil);
    //删除
    int deleteByPrimaryKey(Long configId);
    //批量删除
    int deleteBatch(Long[] ids);
    //通过id查询
    IndexConfig selectByPrimaryKey(Long configId);
    //通过configType类型和id查询
    IndexConfig selectByTypeAndGoodsId(@Param("configType")int configType,@Param("goodsId")Long goodsId);
    //只更新更改的地方
    int updateByPrimaryKeySelective(IndexConfig record);
    //全更新
    int updateByPrimaryKey(IndexConfig record);
    //新增（选择性的）
    int insertSelective(IndexConfig record);
    //新增（全）
    int insert(IndexConfig record);

    //前台查询
    List<IndexConfig> findIndexConfigsByTypeAndNum(@Param("configType")int configType,@Param("number")int number);

}
