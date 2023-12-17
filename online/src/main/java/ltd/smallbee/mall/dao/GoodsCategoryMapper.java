package ltd.smallbee.mall.dao;

import ltd.smallbee.mall.entity.GoodsCategory;
import ltd.smallbee.mall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsCategoryMapper {
    List<GoodsCategory> findGoodsCategoryList(PageQueryUtil pageQueryUtil);

    List<GoodsCategory> selectByLevelAndParentIdsAndNumber(@Param("parentIds") List<Long> parentIds, @Param("categoryLevel") int categoryLevel, @Param("number") int number);

    int getTotalGoodsCategories(PageQueryUtil pageUtil);

    GoodsCategory selectByPrimaryKey(Long categoryId);

    GoodsCategory selectByLevelAndName(@Param("categoryLevel") Byte categoryLevel, @Param("categoryName") String categoryName);

    int deleteByPrimaryKey(Long categoryId);

    int deleteBatch(Integer[] ids);

    int insert(GoodsCategory record);

    int insertSelective(GoodsCategory record);

    int updateByPrimaryKeySelective(GoodsCategory record);

    int updateByPrimaryKey(GoodsCategory record);



}
