// 商品模拟数据
export const products = [
  // 茉莉鲜花
  {
    id: 101,
    name: '横州新鲜茉莉鲜花 500g',
    category: 'fresh',
    categoryName: '茉莉鲜花',
    shopId: 1,
    shopName: '横州茉莉花直供',
    price: 29.90,
    originalPrice: 39.90,
    stock: 100,
    sales: 2560,
    images: [
      'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg',
      'https://cube.elemecdn.com/9/5c/3d2c2d3e2d2d2d2d2d2d2d2d2d2.jpeg',
      'https://cube.elemecdn.com/8/6c/4d4c4c4c4c4c4c4c4c4c4c4c4c.jpeg'
    ],
    specs: [
      { name: '重量', options: ['250g装', '500g装', '1kg装'] }
    ],
    description: `
      <h3>横州新鲜茉莉鲜花</h3>
      <p>精选横州优质茉莉花，清晨采摘，保证新鲜度。花朵饱满，香气浓郁，是制作茉莉花茶、提炼精油的上好原料。</p>
      <h4>产品特点</h4>
      <ul>
        <li>清晨采摘，保持最佳香气</li>
        <li>人工筛选，品质保证</li>
        <li>冷链运输，新鲜到家</li>
        <li>可用于制作花茶、插花、香薰等</li>
      </ul>
      <h4>储存方式</h4>
      <p>建议冷藏保存，3天内食用最佳。</p>
    `,
    reviews: [
      {
        id: 1,
        user: '茉莉***茶',
        avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        content: '花很新鲜，香气浓郁，已经回购好几次了！',
        images: ['https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'],
        rating: 5,
        time: '2026-03-10'
      },
      {
        id: 2,
        user: '爱花***',
        avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        content: '包装很好，物流也快，鲜花很新鲜',
        rating: 4,
        time: '2026-03-08'
      }
    ]
  },
  {
    id: 102,
    name: '横州双瓣茉莉花苗 庭院种植',
    category: 'fresh',
    categoryName: '茉莉鲜花',
    shopId: 1,
    shopName: '横州茉莉花直供',
    price: 18.80,
    originalPrice: 25.00,
    stock: 50,
    sales: 890,
    images: [
      'https://cube.elemecdn.com/5/93/5d5d5d5d5d5d5d5d5d5d5d5d5d.jpeg',
      'https://cube.elemecdn.com/6/44/6d6d6d6d6d6d6d6d6d6d6d6d6d.jpeg'
    ],
    specs: [
      { name: '苗龄', options: ['1年苗', '2年苗', '3年苗'] }
    ],
    description: '<p>优质双瓣茉莉花苗，适合庭院种植，易于养护，香气持久。</p>',
    reviews: []
  },

  // 茉莉花茶
  {
    id: 201,
    name: '横州茉莉花茶 250g 礼盒装',
    category: 'tea',
    categoryName: '茉莉花茶',
    shopId: 2,
    shopName: '茉莉花茶坊',
    price: 128.00,
    originalPrice: 168.00,
    stock: 80,
    sales: 1680,
    images: [
      'https://cube.elemecdn.com/e/58/0d784d0a949e9a39d4c1b0c3bbf8cjpeg.jpeg',
      'https://cube.elemecdn.com/f/69/1d1d1d1d1d1d1d1d1d1d1d1d1d.jpeg',
      'https://cube.elemecdn.com/2/7a/2d2d2d2d2d2d2d2d2d2d2d2d2d.jpeg'
    ],
    specs: [
      { name: '规格', options: ['100g简装', '250g礼盒装', '500g礼盒装'] },
      { name: '等级', options: ['特级', '一级', '二级'] }
    ],
    description: `
      <h3>横州茉莉花茶</h3>
      <p>精选横州优质茶叶与新鲜茉莉花窨制而成，干茶条索紧细，茉莉花香与茶香交融，滋味鲜爽回甘。</p>
      <h4>制作工艺</h4>
      <ul>
        <li>九次窨花工艺</li>
        <li>传统手工制作</li>
        <li>精选春茶原料</li>
      </ul>
      <h4>冲泡方法</h4>
      <p>建议使用85℃水温，投茶3-5克，可冲泡3-5次。</p>
    `,
    reviews: [
      {
        id: 1,
        user: '茶友***',
        avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        content: '口感很好，茉莉花香浓郁，送礼很有面子！',
        rating: 5,
        time: '2026-03-12'
      },
      {
        id: 2,
        user: '品茶***生',
        avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        content: '茶汤清澈，香气扑鼻，回甘很好',
        rating: 5,
        time: '2026-03-09'
      }
    ]
  },
  {
    id: 202,
    name: '横州茉莉银针 100g',
    category: 'tea',
    categoryName: '茉莉花茶',
    shopId: 2,
    shopName: '茉莉花茶坊',
    price: 88.00,
    originalPrice: 108.00,
    stock: 60,
    sales: 980,
    images: [
      'https://cube.elemecdn.com/3/1e/8c0f2d3e8f8c8f8c8f8c8f8c8f8c.jpeg',
      'https://cube.elemecdn.com/4/2f/9d0f3a4e5f6g7h8i9j0k1l2m3n4o5p6.jpeg'
    ],
    specs: [
      { name: '规格', options: ['50g装', '100g装', '200g装'] }
    ],
    description: '<p>茉莉银针采用单芽制作，外形如针，茉莉花香清雅，滋味鲜爽。</p>',
    reviews: []
  },
  {
    id: 203,
    name: '横州茉莉龙珠 礼盒装',
    category: 'tea',
    categoryName: '茉莉花茶',
    shopId: 2,
    shopName: '茉莉花茶坊',
    price: 268.00,
    originalPrice: 328.00,
    stock: 30,
    sales: 560,
    images: [
      'https://cube.elemecdn.com/5/3a/5c5c5c5c5c5c5c5c5c5c5c5c5c.jpeg'
    ],
    specs: [
      { name: '规格', options: ['精品礼盒装', '豪华礼盒装'] }
    ],
    description: '<p>手工搓制成珠状，茉莉花香浓郁持久，礼盒包装精美大气。</p>',
    reviews: []
  },

  // 茉莉文创
  {
    id: 301,
    name: '茉莉花香薰蜡烛 手工定制',
    category: 'creative',
    categoryName: '茉莉文创',
    shopId: 3,
    shopName: '茉莉文创工作室',
    price: 68.00,
    originalPrice: 88.00,
    stock: 120,
    sales: 780,
    images: [
      'https://cube.elemecdn.com/6/4b/6b6b6b6b6b6b6b6b6b6b6b6b6b.jpeg',
      'https://cube.elemecdn.com/7/5c/7c7c7c7c7c7c7c7c7c7c7c7c7c.jpeg'
    ],
    specs: [
      { name: '香型', options: ['茉莉花香', '玫瑰花香', '薰衣草香'] },
      { name: '尺寸', options: ['小号', '中号', '大号'] }
    ],
    description: '<p>手工制作的香薰蜡烛，天然大豆蜡配方，茉莉花香清新怡人，适合家居香氛。</p>',
    reviews: [
      {
        id: 1,
        user: '香氛***',
        avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        content: '味道很好闻，点燃后整个房间都是茉莉花香',
        rating: 5,
        time: '2026-03-11'
      }
    ]
  },
  {
    id: 302,
    name: '茉莉花精油 10ml',
    category: 'creative',
    categoryName: '茉莉文创',
    shopId: 3,
    shopName: '茉莉文创工作室',
    price: 158.00,
    originalPrice: 198.00,
    stock: 45,
    sales: 320,
    images: [
      'https://cube.elemecdn.com/8/7d/8d8d8d8d8d8d8d8d8d8d8d8d8d.jpeg'
    ],
    specs: [
      { name: '规格', options: ['5ml', '10ml', '20ml'] }
    ],
    description: '<p>纯天然茉莉花精油，可用于香薰、按摩、护肤等多种用途。</p>',
    reviews: []
  },
  {
    id: 303,
    name: '茉莉花手工皂 礼盒装',
    category: 'creative',
    categoryName: '茉莉文创',
    shopId: 3,
    shopName: '茉莉文创工作室',
    price: 45.00,
    originalPrice: 58.00,
    stock: 200,
    sales: 1250,
    images: [
      'https://cube.elemecdn.com/9/8e/9e9e9e9e9e9e9e9e9e9e9e9e9e9e.jpeg'
    ],
    specs: [
      { name: '规格', options: ['单块装', '三块礼盒装', '六块礼盒装'] }
    ],
    description: '<p>手工冷制皂，添加茉莉花精华，温和护肤，泡沫丰富。</p>',
    reviews: []
  },
  {
    id: 304,
    name: '茉莉花刺绣工艺品',
    category: 'creative',
    categoryName: '茉莉文创',
    shopId: 3,
    shopName: '茉莉文创工作室',
    price: 188.00,
    originalPrice: 258.00,
    stock: 15,
    sales: 89,
    images: [
      'https://cube.elemecdn.com/1/9f/1f1f1f1f1f1f1f1f1f1f1f1f1f.jpeg'
    ],
    specs: [
      { name: '尺寸', options: ['20cmx20cm', '30cmx30cm', '40cmx40cm'] }
    ],
    description: '<p>纯手工刺绣，精美茉莉花图案，可挂墙装饰或收藏。</p>',
    reviews: []
  }
]

// 获取商品列表（支持分类和搜索）
export const getProducts = (category = '', keyword = '') => {
  let result = [...products]
  if (category && category !== 'all') {
    result = result.filter(p => p.category === category)
  }
  if (keyword) {
    const lowerKeyword = keyword.toLowerCase()
    result = result.filter(p =>
      p.name.toLowerCase().includes(lowerKeyword) ||
      p.categoryName.toLowerCase().includes(lowerKeyword)
    )
  }
  return result
}

// 获取商品详情
export const getProductById = (id) => {
  return products.find(p => p.id === Number(id))
}

// 获取商品分类
export const getCategories = () => {
  return [
    { id: 'fresh', name: '茉莉鲜花', icon: '🌸' },
    { id: 'tea', name: '茉莉花茶', icon: '🍵' },
    { id: 'creative', name: '茉莉文创', icon: '🎁' }
  ]
}
