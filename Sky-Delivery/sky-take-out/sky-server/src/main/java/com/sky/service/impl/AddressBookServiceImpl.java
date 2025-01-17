package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void addAddress(AddressBook addressBook) {
        // 前端传递的参数可能不包含用户id，这里需要手动设置一下
        addressBook.setUserId(BaseContext.getCurrentId());
        // 默认将新增的地址设置为非默认地址
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }


    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    @Override
    public List<AddressBook> list() {
        return addressBookMapper.select(AddressBook.builder().userId(BaseContext.getCurrentId()).build());
    }

    /**
     * 设置默认地址
     * @param addressBook
     */
    @Override
    public void setDefault(AddressBook addressBook) {
        // 1.将当前用户所有的地址设置为非默认地址
        addressBookMapper.updateAllNotDefaultByUserId(BaseContext.getCurrentId());
        // 2.将当前地址设置为默认
        addressBook.setIsDefault(1);
        addressBookMapper.updateById(addressBook);
    }


    /**
     * 查询默认地址
     * @return
     */
    @Override
    public AddressBook getDefault() {
        // 查询当前用户的默认地址
        List<AddressBook> list = addressBookMapper.select(AddressBook.builder().
                userId(BaseContext.getCurrentId()).isDefault(1).build());
        if (list != null && !list.isEmpty()) {
            // 用户的默认地址只有1个
            return list.get(0);
        }
        // 为空说明用户没有设置默认地址，返回null
        return null;
    }


    /**
     * 根据id修改地址信息
     * @param addressBook
     */
    @Override
    public void modifyAddress(AddressBook addressBook) {
        addressBookMapper.updateById(addressBook);
    }


    /**
     * 根据id删除地址
     * @param id
     */
    @Override
    public void delById(Long id) {
        addressBookMapper.deleteById(id);
    }


    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Override
    public AddressBook getById(Long id) {
        List<AddressBook> list = addressBookMapper.select(AddressBook.builder().id(id).build());
        if (list != null && !list.isEmpty()){
            return list.get(0);
        }
        return null;
    }
}
