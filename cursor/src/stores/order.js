import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserOrders, payOrder as apiPayOrder, receiveOrder as apiReceiveOrder, cancelOrder as apiCancelOrder } from '@/api'

// 订单状态映射（数字 → 显示文本）
const STATUS_TEXT = {
  0: '待付款',
  1: '待发货',
  2: '待收货',
  3: '已完成',
  4: '已取消',
  5: '退款中',
  6: '已退款',
  7: '退款驳回'
}

// 状态别名 → 数字
const STATUS_MAP = {
  'all': null,
  'pending': 0,
  'paid': 1,
  'shipped': 2,
  'completed': 3,
  'cancelled': 4,
  'refunding': 5,
  'refunded': 6,
  'rejected': 7
}

export const useOrderStore = defineStore('order', () => {
  const orders = ref([])

  // 统一处理后端订单数据
  const normalizeOrder = (o) => ({
    id: o.id,
    orderNo: o.orderNo,
    shopId: o.shopId,
    status: o.status,
    statusText: STATUS_TEXT[o.status] ?? '未知',
    createTime: o.createTime,
    payTime: o.payTime,
    shipTime: o.shipTime,
    receiveTime: o.receiveTime,
    totalAmount: o.totalAmount,
    freight: o.freight,
    discount: o.discount,
    actualAmount: o.actualAmount,
    receiverName: o.receiverName,
    receiverPhone: o.receiverPhone,
    receiverAddress: o.receiverAddress,
    remark: o.remark,
    items: o.items || []
  })

  // 加载用户订单
  const loadOrders = async (userId) => {
    try {
      const res = await getUserOrders({ userId })
      if (res.code === 200) {
        const records = res.data?.records || []
        orders.value = records.map(normalizeOrder)
      }
    } catch (e) {
      console.error('加载订单失败', e)
      orders.value = []
    }
  }

  // 待付款数量
  const pendingPaymentCount = computed(() => orders.value.filter(o => o.status === 0).length)

  // 待收货数量
  const pendingReceiveCount = computed(() => orders.value.filter(o => o.status === 2).length)

  // 售后中数量（订单表 status=5 退款中）
  const refundingCount = computed(() => orders.value.filter(o => o.status === 5).length)

  // 已退款数量（订单表 status=6 已退款）
  const refundedCount = computed(() => orders.value.filter(o => o.status === 6).length)

  // 按状态筛选
  const getOrdersByStatus = (status) => {
    if (status === 'all') return orders.value
    const num = STATUS_MAP[status]
    if (num === null) return orders.value
    return orders.value.filter(o => o.status === num)
  }

  // 支付订单
  const payOrder = async (orderId, payType = 1) => {
    try {
      const res = await apiPayOrder(orderId, payType)
      if (res.code === 200) {
        const order = orders.value.find(o => o.id === orderId)
        if (order) {
          order.status = 1
          order.statusText = '待发货'
        }
        return true
      }
      return false
    } catch (e) {
      return false
    }
  }

  // 确认收货
  const confirmReceive = async (orderId) => {
    try {
      const res = await apiReceiveOrder(orderId)
      if (res.code === 200) {
        const order = orders.value.find(o => o.id === orderId)
        if (order) {
          order.status = 3
          order.statusText = '已完成'
        }
        return true
      }
      return false
    } catch (e) {
      return false
    }
  }

  // 取消订单
  const cancelOrder = async (orderId) => {
    try {
      const res = await apiCancelOrder(orderId)
      if (res.code === 200) {
        const order = orders.value.find(o => o.id === orderId)
        if (order) {
          order.status = 4
          order.statusText = '已取消'
        }
        return true
      }
      return false
    } catch (e) {
      return false
    }
  }

  // 根据ID获取订单
  const getOrderById = (orderId) => {
    return orders.value.find(o => o.id === orderId) || null
  }

  return {
    orders,
    pendingPaymentCount,
    pendingReceiveCount,
    refundingCount,
    refundedCount,
    loadOrders,
    getOrdersByStatus,
    getOrderById,
    payOrder,
    confirmReceive,
    cancelOrder
  }
})
