// index.js
Page({
  // 定义当前需要使用的数据 json格式
  data: {
    msg: 'hello world',
    nikeName:'',
    url:'',
  },

  //定义方法，获取微信用户的头像和昵称
  getUserInfo(){
    // wx是内置的一个对象，通过调用其getUserProfile方法获取微信用户信息
    wx.getUserProfile({
      desc: '获取用户信息',
      // 获取成功后执行回调函数
      success: (res) =>{
        console.log(res.userInfo)// 打印用户信息
        // 为数据赋值
        this.setData({
          nikeName: res.userInfo.nickName,
          url:res.userInfo.avatarUrl
        })
      }
    })
  }
})
