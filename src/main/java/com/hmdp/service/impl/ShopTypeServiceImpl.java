package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
//    /**
//     * describe 查询商家类型 string类型实现
//     * @return Result
//     */
//    @Override
//    public Result queryTypeList() {
//        //1.先查询redis 如果存在直接返回
//        String shopTypeJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_TYPE_KEY);
////        List<String> list = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);
//        if(BeanUtil.isNotEmpty(shopTypeJson)){
//            List list = JSONUtil.toBean(shopTypeJson, new TypeReference<List<ShopType>>(){},false);
//            list.stream()
//                    .forEach(shopType-> System.out.println(shopType));
//            return Result.ok(list);
//        }
//        //2.redis中不存在数据，查询数据库
//        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
//        String jsonStr = JSONUtil.toJsonStr(shopTypeList);
//        //3.存入redis
//        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_TYPE_KEY,jsonStr,RedisConstants.CACHE_SHOP_TYPE_TTL, TimeUnit.HOURS);
//        return Result.ok(shopTypeList);
//    }

    /**
     * describe 查询商家类型 list类型实现
     * @return Result
     */
    @Override
    public Result queryTypeList() {
        //1.先查询redis 如果存在直接返回
        List<String> list = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);
        if(BeanUtil.isNotEmpty(list) && list.size()>0){
            list.stream()
                    .forEach(type-> System.out.println(type));
            List<ShopType> shopTypeList = list.stream()
                    .map(shopTypeString -> JSONUtil.toBean(shopTypeString, ShopType.class))
                    .collect(Collectors.toList());
            return Result.ok(shopTypeList);
        }
        //2.redis中不存在数据，查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        //opsForList().leftPushAll方法需要List<String>参数
        List<String> stringList = shopTypeList.stream()
                .map(shopTypeObj -> JSONUtil.toJsonStr(shopTypeObj))
                .collect(Collectors.toList());

//        json转List这样也可以
//        List<ShopType> shopTypeList = JSONUtil.toList("", ShopType.class);
        //3.存入redis
        stringRedisTemplate.opsForList().leftPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY,stringList);
        //4.设置过期时间
        stringRedisTemplate.expire(RedisConstants.CACHE_SHOP_TYPE_KEY,RedisConstants.CACHE_SHOP_TYPE_TTL,TimeUnit.HOURS);
        return Result.ok(shopTypeList);
    }
}
