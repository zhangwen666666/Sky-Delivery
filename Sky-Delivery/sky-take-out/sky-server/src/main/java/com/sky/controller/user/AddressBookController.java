package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "C端-地址簿接口")
@RequestMapping("/user/addressBook")
@Slf4j
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    @ApiOperation("新增地址")
    public Result addAddress(@RequestBody AddressBook addressBook){
        addressBookService.addAddress(addressBook);
        return Result.success();
    }


    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> list(){
        List<AddressBook> addresses = addressBookService.list();
        return Result.success(addresses);
    }


    @ApiOperation("设置默认地址")
    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBook addressBook){
        addressBookService.setDefault(addressBook);
        return Result.success();
    }


    @ApiOperation("查询默认地址")
    @GetMapping("/default")
    public Result<AddressBook> getDefault(){
        AddressBook addressBook = addressBookService.getDefault();
        if (addressBook == null) {
            return Result.error("用户未设置默认地址");
        }
        return Result.success(addressBook);
    }

    @ApiOperation("根据id修改地址")
    @PutMapping
    public Result modifyAddress(@RequestBody AddressBook addressBook){
        addressBookService.modifyAddress(addressBook);
        return Result.success();
    }

    @ApiOperation("根据id删除地址")
    @DeleteMapping
    public Result delAddress(Long id){
        addressBookService.delById(id);
        return Result.success();
    }


    @ApiOperation("根据id查询地址")
    @GetMapping("/{id}")
    public Result<AddressBook> getById(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return Result.success(addressBook);
        }
        return Result.error("未查询到地址信息");
    }
}
