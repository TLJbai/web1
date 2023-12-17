package ltd.smallbee.mall.service.impl;

import ltd.smallbee.mall.common.ServiceResultEnum;
import ltd.smallbee.mall.controller.vo.NewBeeMallIndexCarouselVO;
import ltd.smallbee.mall.dao.CarouselMapper;
import ltd.smallbee.mall.entity.Carousel;
import ltd.smallbee.mall.service.MallCarouselService;
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
public class MallCarouselServiceImpl implements MallCarouselService {

    @Autowired
    private CarouselMapper carouselMapper;

    /***
     * 分页显示
     * @param queryPage
     * @return
     */
    @Override
    public PageResult getCarouselByPage(PageQueryUtil queryPage) {
        //获得所有列表
        List<Carousel> list = carouselMapper.selectCarouselByList(queryPage);
        //统计数量
        int total = carouselMapper.getTotalCarousels(queryPage);
        //获取每页的数据
        PageResult pageResult = new PageResult(list,total,queryPage.getLimit(), queryPage.getPage());
        return pageResult;
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @Override
    public Boolean deleteListById(Integer[] ids) {
        //判断ids
        if (ids.length<1)
            return false;
        return carouselMapper.deleteListById(ids)>0;
    }

    /**
     * 通过id查找一个轮播图
     * @param id
     * @return
     */
    @Override
    public Carousel selectOneById(Integer id) {
        return carouselMapper.selectOneById(id);
    }

    /**
     * 更新
     * @param carousel
     * @return
     */
    @Override
    public String updateCarousel(Carousel carousel) {
        Carousel temp = carouselMapper.selectOneById(carousel.getCarouselId());
        if (null == temp){
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        //更新的内容放入temp
        temp.setCarouselRank(carousel.getCarouselRank());
        temp.setRedirectUrl(carousel.getRedirectUrl());
        temp.setCarouselUrl(carousel.getCarouselUrl());
        temp.setUpdateTime(new Date());
        if (carouselMapper.updateByPrimaryKeySelective(temp)>0){
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    /**
     * 新增/保存
     * @param carousel
     * @return
     */
    @Override
    public String saveCarousel(Carousel carousel) {
        if (carouselMapper.insertSelective(carousel) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public List<NewBeeMallIndexCarouselVO> getCarouselsFotIndex(int number) {
        //创建List
        List<NewBeeMallIndexCarouselVO> newBeeMallIndexCarouselVOS = new ArrayList<>(number);
        List<Carousel> carousels = carouselMapper.findCarouselsByNum(number);
        if (!CollectionUtils.isEmpty(carousels)){
            newBeeMallIndexCarouselVOS = BeanUtil.copyList(carousels,NewBeeMallIndexCarouselVO.class);
        }
        return newBeeMallIndexCarouselVOS;
    }

}
