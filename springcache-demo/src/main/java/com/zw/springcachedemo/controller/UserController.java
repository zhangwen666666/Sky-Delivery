package com.zw.springcachedemo.controller;

import com.zw.springcachedemo.entity.User;
import com.zw.springcachedemo.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    // @CachePut有两个属性，cacheNames和key
    // 使用springCache缓存数据，存入redis中的key是：@CachePut中的cacheNames::key
    // 这里需要注意：@CachePut注解中的key属性应该是动态的，否则每次插入数据生成相同的key，会将redis中的记录覆盖掉
    // 可以使用 #方法的参数 来动态设置key的值
    // @CachePut(cacheNames = "userCache", key = "#user.id")
    // 也可以使用 #result 来动态的取到方法的返回值
    // @CachePut(cacheNames = "userCache", key = "#result.id")
    // 还可以使用#p0, #p1或者是#a0, #a1取到方法的第1个、第二个参数
    @CachePut(cacheNames = "userCache", key = "#p0.id")
    public User save(@RequestBody User user){
        userMapper.insert(user);
        return user;
    }

    @DeleteMapping
    public void deleteById(Long id){
        userMapper.deleteById(id);
    }

    @DeleteMapping("/delAll")
    public void deleteAll(){
        userMapper.deleteAll();
    }

    @GetMapping
    public User getById(Long id){
        User user = userMapper.getById(id);
        return user;
    }

}
