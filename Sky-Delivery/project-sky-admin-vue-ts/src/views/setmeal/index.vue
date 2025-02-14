<template>
  <div class="dashboard-container">
    <div class="container">

      <div class="tableBar">
        <label style="margin-right: 5px">套餐名称:</label>
        <el-input v-model="name" style="width:15%" placeholder="请填写套餐名称"/>
        <label style="margin-right: 5px; margin-left: 25px">套餐分类:</label>
        <el-select v-model="categoryId" slot="prepend" placeholder="请选择" style="width:15%">
          <el-option v-for="item in setmealList"
            :key="item.id"
            :label="item.name"
            :value="item.id" />
        </el-select>
        <label style="margin-right: 5px; margin-left: 25px">售卖状态:</label>
        <el-select v-model="status" slot="prepend" placeholder="请选择" style="width:15%">
          <el-option v-for="item in statusArr"
            :key="item.value"
            :label="item.label"
            :value="item.value" />
        </el-select>
        <el-button type="info" style="margin-left:10px" @click="pageQuery">查询</el-button>

        <div class="tableLab">
          <span class="delBut non" @click="deleteHandle('B')">批量删除</span>
          <el-button type="primary" style="margin-left: 15px" @click="() => this.$router.push('/setmeal/add')">+新建套餐</el-button>
        </div>
      </div>

      <!-- 展示数据的表格 -->
      <el-table :data="records" stripe class="tableBox" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="25" />
        <el-table-column prop="name" label="套餐名称" />
        <el-table-column label="图片">
          <template slot-scope="scope">
            <el-image style="width: 80px; height: 40px; border: none" :src="scope.row.image"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="套餐分类" />
        <el-table-column prop="price" label="套餐价"/>
        <el-table-column label="售卖状态">
          <template slot-scope="scope">
            <div class="tableColumn-status" :class="{ 'stop-use': scope.row.status === 0 }">
              {{ scope.row.status === 0 ? '停售' : '启售' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" />
        <el-table-column label="操作" width="250px" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small">修改</el-button>
            <el-button type="text" size="small" @click="handleStartOrStop(scope.row)">
              {{ scope.row.status == '1' ? '停售' : '起售' }}
            </el-button>
            <el-button type="text" size="small" @click="deleteHandle('S', scope.row.id)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页条 -->
      <el-pagination class="pageList"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-sizes="[10, 20, 30, 40, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>
  </div>
</template>

<script lang="ts">
import {getCategoryByType} from '@/api/category'
import {getSetmealPage, enableOrDisableSetmeal, deleteSetmeal} from '@/api/setMeal'
export default {
  data(){
    return {
      name: '', // 套餐名称
      page: 1,
      pageSize: 10,
      total: 0,
      records: [],
      setmealList: [],
      categoryId: '', // 分类Id
      statusArr: [
        {
          value: '0',
          label: '停售'
        },
        {
          value: '1',
          label: '起售'
        }
      ],
      status: '', // 售卖状态
      multipleSelection: '' // 当前表格选中的多个元素
    }
  },

  created(){
    // 根据分类类型查询分类 （套餐分类下拉框数据）
    getCategoryByType({type: 2}).then((res) => {
      if(res.data.code === 1){
        this.setmealList = res.data.data
      }
    })

    // 查询套餐分页数据
    this.pageQuery()
  },

  methods: {
    // 分页查询
    pageQuery(){
      // alert('分页查询')
      // 封装分页查询参数
      const p = {
        page: this.page,
        pageSize: this.pageSize,
        name: this.name,
        status: this.status,
        categoryId: this.categoryId
      }

      getSetmealPage(p).then((res) => {
        if(res.data.code === 1){
          this.total = res.data.data.total
          this.records = res.data.data.records
        }
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

    // 修改套餐销售状态
    handleStartOrStop(row){
      this.$confirm('确认要修改当前套餐的状态吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
      }).then(() => {
        enableOrDisableSetmeal({id: row.id, status: row.status === 1 ? 0 : 1}).then((res) => {
          if(res.data.code === 1){
            this.$message.success('套餐状态修改成功!')
            this.pageQuery()
          }
        }).catch((msg) => {
          this.$message.error(msg)
        })
      })
    },

    // （批量）删除套餐
    deleteHandle(type: string, id: string){
      this.$confirm('确认要删除所选套餐吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
      }).then(() => {
        let param = '' // 保存最终调用删除接口时所传的参数
        if (type === 'B'){
          // 批量删除
          // alert(this.multipleSelection.length)
          const arr = new Array
          this.multipleSelection.forEach((element) => {
            arr.push(element.id)
          })
          param = arr.join(',')
        } else {
          // 单一删除
          // alert('单一删除')
          param = id
        }
        // 调用删除接口
        deleteSetmeal(param).then((res) => {
          if (res.data.code === 1) {
            this.$message.success('删除成功!')
            this.pageQuery()
          } else{
            this.$message.error(res.data.msg)
          }
        })
      })
    },

    // 处理选中的数据赋值给模型，用于批量处理
    handleSelectionChange(val){
      this.multipleSelection = val
      // console.log('当前表格勾选了几项：' + val.length);
      // val.forEach(element => {
      //   console.log(element.id);
      // });
    },
  }
}
</script>
<style lang="scss">
.el-table-column--selection .cell {
  padding-left: 10px;
}
</style>
<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;

    .container {
      background: #fff;
      position: relative;
      z-index: 1;
      padding: 30px 28px;
      border-radius: 4px;

      .tableBar {
        margin-bottom: 20px;
        .tableLab {
          float: right;
          span {
            cursor: pointer;
            display: inline-block;
            font-size: 14px;
            padding: 0 20px;
            color: $gray-2;
          }
        }
      }

      .tableBox {
        width: 100%;
        border: 1px solid $gray-5;
        border-bottom: 0;
      }

      .pageList {
        text-align: center;
        margin-top: 30px;
      }
      //查询黑色按钮样式
      .normal-btn {
        background: #333333;
        color: white;
        margin-left: 20px;
      }
    }
  }
}
</style>
