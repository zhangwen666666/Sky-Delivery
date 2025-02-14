<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 5px">员工姓名:</label>
        <el-input v-model="name" placeholder="请输入员工姓名" style="width: 15%"/>
        <el-button type="primary" style="margin-left: 20px" @click="pageQuery">查询</el-button>
        <el-button type="primary" style="float: right" @click="handleAddEmp">+添加员工</el-button>
      </div>
      
      <!-- 展示数据的表格 -->
      <el-table
        :data="records"
        stripe
        style="width: 100%">
          <el-table-column
            prop="name"
            label="员工姓名"
            width="180">
          </el-table-column>
          <el-table-column
            prop="username"
            label="账号"
            width="180">
          </el-table-column>
          <el-table-column
            prop="phone"
            label="手机号">
          </el-table-column>
          <el-table-column
            prop="status"
            label="账号状态">
            <template slot-scope="scope">
              {{scope.row.status === 0 ? '禁用' : '启用'}}
            </template>
          </el-table-column>
          <el-table-column
            prop="updateTime"
            label="最后操作时间">
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button type="text" @click="handleUpdateEmp(scope.row)">修改</el-button>
              <el-button type="text" @click="handleStartOrStop(scope.row)">
                {{scope.row.status === 0 ? '启用' : '禁用'}}
              </el-button>
            </template>
          </el-table-column>
      </el-table>

      <!-- 分页条 -->
      <el-pagination class="pageList"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="page"
        :page-sizes="[10, 20, 30, 40, 50]"
        :page-size="100"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>
  </div>
</template>
<script lang="ts">
import {getEmployeeList, enableOrDisableEmployee} from '@/api/employee'
export default  {
  // 模型数据
  data(){
    return {
      name: '',
      page: 1,
      pageSize: 10,
      total: 0,
      records: []
    }
  },

  created(){
    this.pageQuery()
  },

  methods: {
    // 分页查询
    pageQuery(){
      // alert("分页查询")
      // 准备请求参数
      const params = {name: this.name, page:this.page, pageSize: this.pageSize}
      // 发送Ajax请求，访问后端服务，获取分页数据
      getEmployeeList(params).then((res) => {
        if (res.data.code === 1){
          this.total = res.data.data.total
          this.records = res.data.data.records
        }
      }).catch((err) => {
        this.$message.console.error('请求出错了: ' + err.message);
      })
    },

    // pageSize发生变化时触发
    handleSizeChange(pageSize){
      this.pageSize = pageSize
      this.pageQuery()
    },

    // page发生变化时触发
    handleCurrentChange(page){
      this.page = page
      this.pageQuery()
    },

    // 启用禁用员工账号
    handleStartOrStop(row){
      if(row.username === 'admin'){
        this.$message.error('admin是系统管理员，不能修改其状态')
        return
      }

      // 弹出确认提示框
      this.$confirm('确认要修改当前员工的状态吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
      }).then(
        () => {
          // alert(`id=${row.id} status=${row.status}`)
          // 当前状态值是1，则传入0.当前状态值是0，则传入1
          const p = {id: row.id, status: row.status === 1 ? 0 : 1}
          enableOrDisableEmployee(p).then(res => {
            if(res.data.code === 1) {
              this.$message.success('员工账号状态修改成功!')
              this.pageQuery()
            }
          })   
        }
      )
    },

    // 跳转到新增员工页面
    handleAddEmp(){
      // 路由跳转, 跳转到新增员工组件
      this.$router.push('/employee/add')
    },

    // 跳转到修改员工页面
    handleUpdateEmp(row){
      if(row.username === 'admin'){
        this.$message.error('管理员的信息不能修改')
        return
      }
      this.$router.push({
        path: '/employee/add',
        query: {id: row.id}
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.disabled-text {
  color: #bac0cd !important;
}
</style>
