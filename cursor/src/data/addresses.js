// 地址管理数据

export const addresses = [
  {
    id: 1,
    name: '王小明',
    phone: '138****8888',
    province: '广西',
    city: '南宁市',
    district: '横州市',
    detail: '横州镇茉莉花大道88号',
    isDefault: true,
    tag: '家'
  },
  {
    id: 2,
    name: '王小明',
    phone: '139****9999',
    province: '广西',
    city: '南宁市',
    district: '青秀区',
    detail: '民族大道123号',
    isDefault: false,
    tag: '公司'
  }
]

// 获取地址列表
export const getAddresses = () => addresses

// 获取默认地址
export const getDefaultAddress = () => addresses.find(a => a.isDefault)

// 获取地址详情
export const getAddressById = (id) => addresses.find(a => a.id === Number(id))

// 添加地址
export const addAddress = (address) => {
  const newId = Math.max(...addresses.map(a => a.id)) + 1
  addresses.push({
    id: newId,
    ...address,
    isDefault: address.isDefault || false
  })
  return newId
}

// 更新地址
export const updateAddress = (id, data) => {
  const index = addresses.findIndex(a => a.id === Number(id))
  if (index > -1) {
    addresses[index] = { ...addresses[index], ...data }
    return true
  }
  return false
}

// 删除地址
export const deleteAddress = (id) => {
  const index = addresses.findIndex(a => a.id === Number(id))
  if (index > -1) {
    addresses.splice(index, 1)
    return true
  }
  return false
}

// 设为默认地址
export const setDefaultAddress = (id) => {
  addresses.forEach(a => {
    a.isDefault = a.id === Number(id)
  })
}
