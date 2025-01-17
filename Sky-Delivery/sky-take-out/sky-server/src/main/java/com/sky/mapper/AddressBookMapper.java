package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressBookMapper {
    /**
     * 新增地址
     * @param addressBook
     */
    @Insert("insert into address_book(city_code, city_name, consignee, detail, district_code, district_name, is_default, label, phone, province_code, province_name, sex, user_id)" +
            "values(#{cityCode}, #{cityName}, #{consignee}, #{detail}, #{districtCode}, #{districtName}, #{isDefault}, #{label}, #{phone}, #{provinceCode}, #{provinceName}, #{sex}, #{userId})")
    void insert(AddressBook addressBook);

    /**
     * 条件查询(id、userId，phone，isDefault)
     * @param addressBook
     * @return
     */
    List<AddressBook> select(AddressBook addressBook);

    /**
     * 根据id修改地址信息
     * @param addressBook
     */
    void updateById(AddressBook addressBook);

    /**
     * 将当前用户的所有地址设置为非默认
     * @param userId
     */
    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void updateAllNotDefaultByUserId(Long userId);

    /**
     * 根据id删除地址
     * @param id
     */
    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);
}
