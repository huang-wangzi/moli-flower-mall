/**
 * 商家资质API
 * 提供商家资质相关的接口调用
 */
import { api } from './client.js'

/**
 * 获取我的资质申请
 * @param {number} userId - 用户ID
 * @returns {Promise} 资质申请信息
 */
export const getMyQualification = (userId) => {
  return api.get('/shop/qualification/my', { params: { userId } })
}

/**
 * 获取待审核的资质列表（管理员）
 * @returns {Promise} 待审核列表
 */
export const getQualificationPending = () => {
  return api.get('/shop/qualification/pending')
}

/**
 * 获取所有资质记录（管理员）
 * @returns {Promise} 所有记录列表
 */
export const getQualificationAll = () => {
  return api.get('/shop/qualification/all')
}

/**
 * 提交资质申请
 * @param {Object} data - 资质申请数据
 * @param {number} data.userId - 用户ID
 * @param {string} data.shopName - 店铺名称
 * @param {string} data.contact - 联系电话
 * @param {string} data.businessLicense - 营业执照（URL或Base64）
 * @param {string} data.idCardFront - 身份证正面（URL或Base64）
 * @param {string} data.idCardBack - 身份证背面（URL或Base64）
 * @param {string} [data.qualityCert] - 品质认证（可选）
 * @returns {Promise} 提交结果
 */
export const submitQualification = (data) => {
  return api.post('/shop/qualification/apply', data)
}

/**
 * 审核资质申请（管理员）
 * @param {number} id - 资质申请ID
 * @param {number} status - 状态: 1-通过 2-拒绝
 * @param {string} [rejectReason] - 拒绝原因
 * @param {number} auditBy - 审核人ID
 * @returns {Promise} 审核结果
 */
export const auditQualification = (id, status, rejectReason, auditBy) => {
  return api.put('/shop/qualification/audit', null, { 
    params: { id, status, rejectReason, auditBy } 
  })
}

/**
 * 上传资质图片
 * @param {File} file - 图片文件
 * @returns {Promise} { url: string } 图片URL
 */
export const uploadQualificationImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export default {
  getMyQualification,
  getQualificationPending,
  getQualificationAll,
  submitQualification,
  auditQualification,
  uploadQualificationImage
}
