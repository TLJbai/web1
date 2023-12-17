package ltd.smallbee.mall.dao;

import ltd.smallbee.mall.entity.Carousel;
import ltd.smallbee.mall.util.PageQueryUtil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CarouselMapper {
    //获取轮播图内容的总数
    int getTotalCarousels(PageQueryUtil pageQuery);
    //分页整合轮播图的内容
    List<Carousel> selectCarouselByList(PageQueryUtil pageQuery);
    //删除(批量删除)
    int deleteListById(Integer[] ids);
    //通过id选择轮播图
    Carousel selectOneById(Integer id);
    //修改选择的轮播图
    int updateByPrimaryKeySelective(Carousel record);
    //更新
    int updateByPrimaryKey(Carousel record);
    //新增
    int insert(Carousel record);
    //选择性新增
    int insertSelective(Carousel record);
    //轮播图
    List<Carousel> findCarouselsByNum(@Param("number") int number);

}
