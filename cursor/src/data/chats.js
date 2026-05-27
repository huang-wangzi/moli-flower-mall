// 聊天记录数据

export const chatList = [
  {
    id: 1,
    type: 'shop', // shop:商家, service:客服
    name: '横州茉莉花直供',
    avatar: 'https://cube.elemecdn.com/9/5c/3d2c2d3e2d2d2d2d2d2d2d2d2d.jpeg',
    lastMessage: '感谢您的购买，欢迎下次光临！',
    lastTime: '2026-03-13 18:20:00',
    timeAgo: '昨天',
    unread: 0,
    messages: [
      {
        id: 1,
        sender: 'shop',
        content: '您好，欢迎光临横州茉莉花直供！有什么可以帮您的吗？',
        time: '2026-03-13 10:00:00'
      },
      {
        id: 2,
        sender: 'user',
        content: '你好，我想问一下茉莉鲜花怎么保存比较久？',
        time: '2026-03-13 10:05:00'
      },
      {
        id: 3,
        sender: 'shop',
        content: '您好，建议您收到花后放入冰箱冷藏，温度控制在2-5℃，可以保存3-5天。如果是泡茶用，建议24小时内使用完，香气最佳。',
        time: '2026-03-13 10:10:00'
      },
      {
        id: 4,
        sender: 'user',
        content: '好的，谢谢！那给我下单500g的茉莉鲜花吧',
        time: '2026-03-13 10:15:00'
      },
      {
        id: 5,
        sender: 'shop',
        content: '好的，已经为您下单，订单号：HJ2026031312345678，我们会尽快发货！',
        time: '2026-03-13 10:20:00'
      },
      {
        id: 6,
        sender: 'user',
        content: '好的，谢谢！',
        time: '2026-03-13 18:15:00'
      },
      {
        id: 7,
        sender: 'shop',
        content: '感谢您的购买，欢迎下次光临！',
        time: '2026-03-13 18:20:00'
      }
    ]
  },
  {
    id: 2,
    type: 'service',
    name: '官方客服',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    lastMessage: '您的问题已解决，请对我的服务进行评价~',
    lastTime: '2026-03-12 15:30:00',
    timeAgo: '3天前',
    unread: 0,
    messages: [
      {
        id: 1,
        sender: 'user',
        content: '你好，我想咨询一下退货流程',
        time: '2026-03-12 15:00:00'
      },
      {
        id: 2,
        sender: 'service',
        content: '您好，很高兴为您服务！请问您是因为什么要退货呢？',
        time: '2026-03-12 15:05:00'
      },
      {
        id: 3,
        sender: 'user',
        content: '商品质量有问题，想申请退款',
        time: '2026-03-12 15:10:00'
      },
      {
        id: 4,
        sender: 'service',
        content: '非常抱歉给您带来了不好的体验。您可以进入"我的订单"，找到对应订单，点击"申请售后"填写退款原因即可。我们的客服会在24小时内处理您的申请。',
        time: '2026-03-12 15:20:00'
      },
      {
        id: 5,
        sender: 'user',
        content: '好的，明白了，谢谢',
        time: '2026-03-12 15:25:00'
      },
      {
        id: 6,
        sender: 'service',
        content: '不客气！您的问题已解决，请对我的服务进行评价~',
        time: '2026-03-12 15:30:00'
      }
    ]
  },
  {
    id: 3,
    type: 'shop',
    name: '茉莉花茶坊',
    avatar: 'https://cube.elemecdn.com/e/58/0d784d0a949e9a39d4c1b0c3bbf8cjpeg.jpeg',
    lastMessage: '亲，您下单的茉莉花茶我们已经打包好了~',
    lastTime: '2026-03-14 09:00:00',
    timeAgo: '30分钟前',
    unread: 1,
    messages: [
      {
        id: 1,
        sender: 'shop',
        content: '您好，茉莉花茶坊为您提供优质的茉莉花茶~',
        time: '2026-03-14 08:30:00'
      },
      {
        id: 2,
        sender: 'user',
        content: '我想买一些送人，有没有礼盒装的？',
        time: '2026-03-14 08:35:00'
      },
      {
        id: 3,
        sender: 'shop',
        content: '有的，我们有250g和500g的礼盒装，送人非常体面！',
        time: '2026-03-14 08:40:00'
      },
      {
        id: 4,
        sender: 'user',
        content: '好的，给我来一盒250g礼盒装的',
        time: '2026-03-14 08:45:00'
      },
      {
        id: 5,
        sender: 'shop',
        content: '好的，已为您下单，订单号：HJ2026031412345679',
        time: '2026-03-14 08:50:00'
      },
      {
        id: 6,
        sender: 'shop',
        content: '亲，您下单的茉莉花茶我们已经打包好了~',
        time: '2026-03-14 09:00:00'
      }
    ]
  }
]

// 获取聊天列表
export const getChatList = () => chatList

// 获取聊天详情
export const getChatById = (id) => {
  return chatList.find(c => c.id === Number(id))
}

// 发送消息
export const sendMessage = (chatId, content) => {
  const chat = chatList.find(c => c.id === Number(chatId))
  if (chat) {
    const newMessage = {
      id: Date.now(),
      sender: 'user',
      content,
      time: new Date().toLocaleString()
    }
    chat.messages.push(newMessage)
    chat.lastMessage = content
    chat.lastTime = newMessage.time
    return newMessage
  }
  return null
}

// 标记聊天为已读
export const markChatAsRead = (chatId) => {
  const chat = chatList.find(c => c.id === Number(chatId))
  if (chat) {
    chat.unread = 0
  }
}
