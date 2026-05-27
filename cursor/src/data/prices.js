// 价格监测数据

// 茉莉花价格数据（30天走势）
export const priceData = {
  fresh: {
    name: '茉莉鲜花',
    currentPrice: 28.50,
    yesterdayPrice: 27.80,
    change: 2.52, // 涨跌幅度%
    avgPrice: 26.80,
    highestPrice: 32.00,
    lowestPrice: 22.50,
    unit: '元/斤',
    trend: [ // 30天价格数据
      { date: '02-13', price: 24.50 },
      { date: '02-14', price: 25.00 },
      { date: '02-15', price: 25.20 },
      { date: '02-16', price: 24.80 },
      { date: '02-17', price: 25.50 },
      { date: '02-18', price: 26.00 },
      { date: '02-19', price: 26.50 },
      { date: '02-20', price: 27.00 },
      { date: '02-21', price: 27.20 },
      { date: '02-22', price: 26.80 },
      { date: '02-23', price: 26.50 },
      { date: '02-24', price: 27.00 },
      { date: '02-25', price: 27.50 },
      { date: '02-26', price: 28.00 },
      { date: '02-27', price: 28.50 },
      { date: '02-28', price: 29.00 },
      { date: '03-01', price: 30.00 },
      { date: '03-02', price: 31.00 },
      { date: '03-03', price: 32.00 },
      { date: '03-04', price: 31.50 },
      { date: '03-05', price: 30.50 },
      { date: '03-06', price: 29.80 },
      { date: '03-07', price: 29.00 },
      { date: '03-08', price: 28.50 },
      { date: '03-09', price: 28.00 },
      { date: '03-10', price: 27.50 },
      { date: '03-11', price: 27.80 },
      { date: '03-12', price: 27.80 },
      { date: '03-13', price: 28.20 },
      { date: '03-14', price: 28.50 }
    ],
    marketPrices: [ // 各市场价格对比
      { market: '横州花市场', price: 28.50, change: 2.52 },
      { market: '南宁花市', price: 30.00, change: 1.69 },
      { market: '柳州花市', price: 29.50, change: 0.68 },
      { market: '桂林花市', price: 31.00, change: 3.33 },
      { market: '玉林花市', price: 28.00, change: -1.41 }
    ]
  },
  tea: {
    name: '茉莉花茶',
    currentPrice: 156.00,
    yesterdayPrice: 152.00,
    change: 2.63,
    avgPrice: 148.00,
    highestPrice: 168.00,
    lowestPrice: 138.00,
    unit: '元/斤',
    trend: [
      { date: '02-13', price: 142.00 },
      { date: '02-14', price: 144.00 },
      { date: '02-15', price: 145.00 },
      { date: '02-16', price: 143.00 },
      { date: '02-17', price: 146.00 },
      { date: '02-18', price: 148.00 },
      { date: '02-19', price: 150.00 },
      { date: '02-20', price: 152.00 },
      { date: '02-21', price: 154.00 },
      { date: '02-22', price: 152.00 },
      { date: '02-23', price: 150.00 },
      { date: '02-24', price: 152.00 },
      { date: '02-25', price: 155.00 },
      { date: '02-26', price: 158.00 },
      { date: '02-27', price: 160.00 },
      { date: '02-28', price: 162.00 },
      { date: '03-01', price: 165.00 },
      { date: '03-02', price: 168.00 },
      { date: '03-03', price: 166.00 },
      { date: '03-04', price: 164.00 },
      { date: '03-05', price: 162.00 },
      { date: '03-06', price: 160.00 },
      { date: '03-07', price: 158.00 },
      { date: '03-08', price: 156.00 },
      { date: '03-09', price: 154.00 },
      { date: '03-10', price: 153.00 },
      { date: '03-11', price: 154.00 },
      { date: '03-12', price: 155.00 },
      { date: '03-13', price: 155.00 },
      { date: '03-14', price: 156.00 }
    ],
    marketPrices: [
      { market: '横州茶叶市场', price: 156.00, change: 2.63 },
      { market: '南宁茶城', price: 162.00, change: 1.89 },
      { market: '柳州茶市', price: 158.00, change: 1.28 },
      { market: '桂林茶市', price: 165.00, change: 3.12 },
      { market: '玉林茶市', price: 152.00, change: -0.65 }
    ]
  },
  creative: {
    name: '茉莉文创',
    currentPrice: 85.00,
    yesterdayPrice: 82.00,
    change: 3.66,
    avgPrice: 78.00,
    highestPrice: 98.00,
    lowestPrice: 65.00,
    unit: '元/件',
    trend: [
      { date: '02-13', price: 68.00 },
      { date: '02-14', price: 70.00 },
      { date: '02-15', price: 72.00 },
      { date: '02-16', price: 70.00 },
      { date: '02-17', price: 73.00 },
      { date: '02-18', price: 75.00 },
      { date: '02-19', price: 76.00 },
      { date: '02-20', price: 78.00 },
      { date: '02-21', price: 80.00 },
      { date: '02-22', price: 78.00 },
      { date: '02-23', price: 76.00 },
      { date: '02-24', price: 78.00 },
      { date: '02-25', price: 80.00 },
      { date: '02-26', price: 82.00 },
      { date: '02-27', price: 85.00 },
      { date: '02-28', price: 88.00 },
      { date: '03-01', price: 92.00 },
      { date: '03-02', price: 98.00 },
      { date: '03-03', price: 95.00 },
      { date: '03-04', price: 92.00 },
      { date: '03-05', price: 90.00 },
      { date: '03-06', price: 88.00 },
      { date: '03-07', price: 86.00 },
      { date: '03-08', price: 84.00 },
      { date: '03-09', price: 82.00 },
      { date: '03-10', price: 81.00 },
      { date: '03-11', price: 82.00 },
      { date: '03-12', price: 84.00 },
      { date: '03-13', price: 84.00 },
      { date: '03-14', price: 85.00 }
    ],
    marketPrices: [
      { market: '横州文创店', price: 85.00, change: 3.66 },
      { market: '南宁文创店', price: 92.00, change: 2.22 },
      { market: '柳州文创店', price: 88.00, change: 1.15 },
      { market: '桂林文创店', price: 95.00, change: 3.26 },
      { market: '玉林文创店', price: 80.00, change: -1.23 }
    ]
  }
}

// 获取价格数据
export const getPriceData = (category = '') => {
  if (category && category !== 'all') {
    return priceData[category]
  }
  return priceData
}

// 获取所有分类价格概览
export const getPriceOverview = () => {
  return [
    {
      id: 'fresh',
      name: '茉莉鲜花',
      icon: '🌸',
      currentPrice: priceData.fresh.currentPrice,
      change: priceData.fresh.change,
      unit: priceData.fresh.unit
    },
    {
      id: 'tea',
      name: '茉莉花茶',
      icon: '🍵',
      currentPrice: priceData.tea.currentPrice,
      change: priceData.tea.change,
      unit: priceData.tea.unit
    },
    {
      id: 'creative',
      name: '茉莉文创',
      icon: '🎁',
      currentPrice: priceData.creative.currentPrice,
      change: priceData.creative.change,
      unit: priceData.creative.unit
    }
  ]
}
