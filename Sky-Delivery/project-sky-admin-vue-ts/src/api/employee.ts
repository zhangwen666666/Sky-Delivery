import request from '@/utils/request'
/**
 *
 * 员工管理
 *
 **/
// 登录
export const login = (data: any) =>
  request({
    'url': '/employee/login',
    'method': 'post',
    data: data
  })

  // 退出
 export const userLogout = (params: any) =>
 request({
   'url': `/employee/logout`,
   'method': 'post',
   params 
 })

 // 分页查询
export const getEmployeeList = (params: any) =>
  request({
    'url': `/employee/page`,
    'method': 'get',
    'params': params
})


  