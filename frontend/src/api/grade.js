import request from '@/utils/request'

/**
 * 成绩查询（GET，从后端 MongoDB/教务系统获取）
 * @param {number} [year] 可选，学年索引 0~3（大一~大四），不传则默认最近学年
 * @returns {Promise<{ success: boolean, data: { year, firstTermGPA, secondTermGPA, firstTermGradeList, secondTermGradeList } }>}
 */
export function getGrade(year) {
  const config = year != null ? { params: { year } } : {}
  return request.get('/grade', config)
}

/**
 * 强制刷新当前用户的成绩缓存（POST /api/grade/update）
 * 后端会根据是否为测试账号返回不同结果，错误提示由全局拦截器处理。
 */
export function updateGradeCache() {
  return request.post('/grade/update')
}

