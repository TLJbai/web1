package ltd.smallbee.mall.service;


import ltd.smallbee.mall.controller.vo.NewBeeMallIndexCarouselVO;
import ltd.smallbee.mall.entity.Carousel;
import ltd.smallbee.mall.util.PageQueryUtil;
import ltd.smallbee.mall.util.PageResult;

import java.util.List;

public interface MallCarouselService {
    //内容列表
    PageResult getCarouselByPage(PageQueryUtil queryUtil);
    //删除轮播图中的素材
    Boolean deleteListById(Integer[] ids);
    //通过id选择轮播图
    Carousel selectOneById(Integer id);
    //修改选择的轮播图
    String updateCarousel(Carousel carousel);
    //新增
    String saveCarousel(Carousel carousel);

    List<NewBeeMallIndexCarouselVO> getCarouselsFotIndex(int number);




}
