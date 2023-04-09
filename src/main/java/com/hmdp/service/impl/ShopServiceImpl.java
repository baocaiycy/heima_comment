package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;



/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * describe 通过商户id查询详情
     * @param id 商户id
     * @return Result
     */
    @Override
    public Result queryShopById(Long id) {
        //1.先在redis中查询
        String shopJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
        //2.redis有该数据，直接返回
        if(BeanUtil.isNotEmpty(shopJson)){
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        //3.redis中没有数据，查询数据库
        Shop shop = query().eq("id", id).one();
        if(BeanUtil.isEmpty(shop)){
            return Result.fail("不存在该商家");
        }
        //4.将数据库结果写入redis
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY+id,JSONUtil.toJsonStr(shop),RedisConstants.CACHE_SHOP_TTL,TimeUnit.MINUTES);
        return Result.ok(shop);
    }

    /**
     * describe 通过id修改商户
     * @param shop
     * @return Result
     */
    @Override
    @Transactional
    public Result updateShopById(Shop shop) {
        if(BeanUtil.isEmpty(shop) || BeanUtil.isEmpty(shop.getId())){
            return Result.fail("请传入正确的shop_id");
        }
        //1.修改数据库中商户信息
        boolean updateShopSign = updateById(shop);
        //2.删除redis中缓存的商户信息
        Boolean deleteRedisShop = stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());

        return Result.ok();
    }
}
