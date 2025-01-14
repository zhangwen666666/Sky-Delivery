// index.js
Page({
  // 定义当前需要使用的数据 json格式
  data: {
    msg: 'hello world',
    nikeName:'',
    url:'',
    code:'',
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
  },

  // 微信登录，获取微信用户的授权码
  // 每次点击拿到的授权码是不一样的，一个授权码只能使用一次
  // 通过授权码可以请求微信服务器，获取微信用户的openID
  userLogin(){
    wx.login({
      success: (res) => {
        console.log(res.code) // 打印授权码
        this.setData({
          code: res.code
        })
      },
    })
  },

  // 发送请求，请求后端的接口
  sendRequest(){
    wx.request({
      url: 'http://111.119.211.126/user/shop/status',
      method: 'GET',
      success: (res) => {
        console.log(res.data)
      }
    })
  }
})
