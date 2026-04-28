import { beforeEach, describe, expect, it } from 'vitest'

import { handleRequest } from './index'
import { MOCK_ACCOUNT_DATA } from './mock-data'

let storage = {}

beforeEach(() => {
  storage = {}
  Object.defineProperty(globalThis, 'localStorage', {
    configurable: true,
    value: {
      getItem(key) {
        return Object.prototype.hasOwnProperty.call(storage, key) ? storage[key] : null
      },
      setItem(key, value) {
        storage[key] = String(value)
      },
      removeItem(key) {
        delete storage[key]
      },
    },
  })
})

async function request(path, options = {}) {
  const response = await handleRequest({
    path,
    method: options.method || 'GET',
    data: options.data,
    sessionToken: options.token,
    locale: 'zh-CN',
  })

  expect(response.success, path).toBe(true)
  expect(response.code, path).toBe(200)
  return response
}

async function login(data = {}) {
  const response = await request('/api/auth/login', {
    method: 'POST',
    data: {
      username: MOCK_ACCOUNT_DATA.username,
      password: MOCK_ACCOUNT_DATA.password,
      campusCredentialConsent: true,
      policyDate: '2026-04-25',
      effectiveDate: '2026-05-11',
      ...data,
    },
  })

  expect(response.data.token).toBeTruthy()
  return response.data.token
}

describe('mock smoke', () => {
  it('covers auth, profile and account center flows', async () => {
    const token = await login()

    const profile = await request('/api/user/profile', { token })
    expect(profile.data.username).toBeTruthy()

    const locations = await request('/api/profile/locations', { token })
    expect(locations.data.length).toBeGreaterThan(0)

    const options = await request('/api/profile/options', { token })
    expect(options.data.faculties.length).toBeGreaterThan(0)

    const privacy = await request('/api/privacy', { token })
    expect(privacy.data).toHaveProperty('cacheAllow')

    const campusCredentialStatus = await request('/api/campus-credential/status', { token })
    expect(campusCredentialStatus.data.hasActiveConsent).toBe(true)
    expect(campusCredentialStatus.data.hasSavedCredential).toBe(true)
    expect(campusCredentialStatus.data.quickAuthEnabled).toBe(true)
    expect(campusCredentialStatus.data.maskedCampusAccount).toBeTruthy()

    const quickAuthOff = await request('/api/campus-credential/quick-auth', {
      method: 'POST',
      token,
      data: { enabled: false },
    })
    expect(quickAuthOff.data.quickAuthEnabled).toBe(false)

    const loginWithoutConsentToken = await login({ campusCredentialConsent: undefined })
    const campusCredentialAfterPlainRelogin = await request('/api/campus-credential/status', {
      token: loginWithoutConsentToken,
    })
    expect(campusCredentialAfterPlainRelogin.data.hasActiveConsent).toBe(true)
    expect(campusCredentialAfterPlainRelogin.data.hasSavedCredential).toBe(true)
    expect(campusCredentialAfterPlainRelogin.data.quickAuthEnabled).toBe(false)

    const reloginToken = await login()
    const campusCredentialAfterRelogin = await request('/api/campus-credential/status', { token: reloginToken })
    expect(campusCredentialAfterRelogin.data.hasActiveConsent).toBe(true)
    expect(campusCredentialAfterRelogin.data.hasSavedCredential).toBe(true)
    expect(campusCredentialAfterRelogin.data.quickAuthEnabled).toBe(false)

    const revoked = await request('/api/campus-credential/revoke', {
      method: 'POST',
      token: reloginToken,
    })
    expect(revoked.data.hasActiveConsent).toBe(false)
    expect(revoked.data.hasSavedCredential).toBe(false)

    const deleted = await request('/api/campus-credential', {
      method: 'DELETE',
      token,
    })
    expect(deleted.data.quickAuthEnabled).toBe(false)

    const phoneStatus = await request('/api/phone/status', { token })
    expect(phoneStatus.data).toHaveProperty('code')

    const emailStatus = await request('/api/email/status', { token })
    expect(emailStatus.data).not.toBeUndefined()

    const loginRecords = await request('/api/ip/start/0/size/20', { token })
    expect(loginRecords.data.length).toBeGreaterThan(0)

    const userdataState = await request('/api/userdata/state', { token })
    expect(typeof userdataState.data).toBe('number')

    const exportStart = await request('/api/userdata/export', {
      method: 'POST',
      token,
    })
    expect(exportStart.data).toBeNull()

    const download = await request('/api/userdata/download', {
      method: 'POST',
      token,
    })
    expect(download.data).toContain('mock-userdata.zip')

    const feedback = await request('/api/feedback', {
      method: 'POST',
      token,
      data: {
        content: 'mock smoke feedback',
        contact: 'tester@example.com',
      },
    })
    expect(feedback.data).toBeNull()
  })

  it('covers academic, campus, info and message flows', async () => {
    const token = await login()

    const grade = await request('/api/grade?year=2025', { token })
    expect(grade.data).toHaveProperty('firstTermGradeList')

    const schedule = await request('/api/schedule?week=6', { token })
    expect(schedule.data.scheduleList.length).toBeGreaterThan(0)

    const cardInfo = await request('/api/card/info', { token })
    expect(cardInfo.data.cardNumber).toBeTruthy()

    const cardQuery = await request('/api/card/query', {
      method: 'POST',
      token,
      data: { date: '2026-03-01' },
    })
    expect(cardQuery.data.cardList.length).toBeGreaterThan(0)

    const librarySearch = await request('/api/library/search?keyword=Swift&page=1')
    expect(librarySearch.data.collectionList.length).toBeGreaterThan(0)

    const libraryDetail = await request('/api/library/detail?detailURL=detail_swiftui')
    expect(libraryDetail.data.bookname).toBeTruthy()

    const borrowed = await request('/api/library/borrow?password=library123', { token })
    expect(borrowed.data.length).toBeGreaterThan(0)

    const renew = await request('/api/library/renew', {
      method: 'POST',
      token,
      data: {
        code: borrowed.data[0].code,
        password: 'library123',
      },
    })
    expect(renew.data).toBeNull()

    const captcha = await request('/api/cet/checkcode', { token })
    expect(captcha.data.length).toBeGreaterThan(20)

    const cet = await request('/api/cet/query?ticketNumber=123456789012345&name=林知远&checkcode=gd26', { token })
    expect(cet.data.totalScore).toBeTruthy()

    const spare = await request('/api/spare/query', {
      method: 'POST',
      token,
      data: { zone: 0, type: 0, classNumber: 1 },
    })
    expect(spare.data.length).toBeGreaterThan(0)

    const graduateExam = await request('/api/graduate-exam/query', {
      method: 'POST',
      data: {
        name: '林知远',
        examNumber: '441526010203',
        idNumber: '440101200409160011',
      },
    })
    expect(graduateExam.data.totalScore).toBeTruthy()

    const electricity = await request('/api/data/electricfees', {
      method: 'POST',
      data: {
        year: 2026,
        name: '林知远',
        number: '20231234567',
      },
    })
    expect(electricity.data.totalElectricBill).toBeTruthy()

    const yellowPage = await request('/api/data/yellowpage')
    expect(yellowPage.data.data.length).toBeGreaterThan(0)

    const overview = await request('/api/information/overview')
    expect(overview.data.festival.name).toBeTruthy()

    const moduleState = await request('/api/module/state/detail')
    expect(moduleState.data.extension.NEWS).toBe(true)

    const announcements = await request('/api/information/announcement/start/0/size/10', { token })
    expect(announcements.data.length).toBeGreaterThan(0)

    const announcementDetail = await request(`/api/information/announcement/id/${announcements.data[0].id}`, { token })
    expect(announcementDetail.data.title).toBeTruthy()

    const news = await request('/api/information/news/type/1/start/0/size/10')
    expect(news.data.length).toBeGreaterThan(0)

    const newsDetail = await request(`/api/information/news/id/${news.data[0].id}`)
    expect(newsDetail.data.title).toBeTruthy()

    const interactions = await request('/api/information/message/interaction/start/0/size/10', { token })
    expect(interactions.data.length).toBeGreaterThan(0)

    const unread = await request('/api/information/message/unread', { token })
    expect(unread.data).toBeGreaterThanOrEqual(0)

    const readOne = await request(`/api/information/message/id/${interactions.data[0].id}/read`, {
      method: 'POST',
      token,
    })
    expect(readOne.data).toBeNull()

    const readAll = await request('/api/information/message/readall', {
      method: 'POST',
      token,
    })
    expect(readAll.data).toBeNull()
  })

  it('covers community feature flows', async () => {
    const token = await login()

    const marketplace = await request('/api/ershou/item/start/0', { token })
    expect(marketplace.data.length).toBeGreaterThan(0)
    const marketplaceDetail = await request(`/api/ershou/item/id/${marketplace.data[0].id}`, { token })
    expect(marketplaceDetail.data.secondhandItem.id).toBe(marketplace.data[0].id)
    const marketplaceProfile = await request('/api/ershou/profile', { token })
    expect(marketplaceProfile.data).toHaveProperty('doing')

    const lostFound = await request('/api/lostandfound/lostitem/start/0', { token })
    expect(lostFound.data.length).toBeGreaterThan(0)
    const lostFoundDetail = await request(`/api/lostandfound/item/id/${lostFound.data[0].id}`, { token })
    expect(lostFoundDetail.data.item.id).toBe(lostFound.data[0].id)

    const secret = await request('/api/secret/info/start/0/size/10', { token })
    expect(secret.data.length).toBeGreaterThan(0)
    const secretDetail = await request(`/api/secret/id/${secret.data[0].id}`, { token })
    expect(secretDetail.data.id).toBe(secret.data[0].id)
    const secretComments = await request(`/api/secret/id/${secret.data[0].id}/comments`, { token })
    expect(Array.isArray(secretComments.data)).toBe(true)

    const dating = await request('/api/dating/profile/area/0/start/0', { token })
    expect(dating.data.length).toBeGreaterThan(0)
    const datingDetail = await request(`/api/dating/profile/id/${dating.data[0].profileId}`, { token })
    expect(datingDetail.data.profile.profileId).toBe(dating.data[0].profileId)
    const datingMine = await request('/api/dating/profile/my', { token })
    expect(Array.isArray(datingMine.data)).toBe(true)

    const express = await request('/api/express/start/0/size/10', { token })
    expect(express.data.length).toBeGreaterThan(0)
    const expressDetail = await request(`/api/express/id/${express.data[0].id}`, { token })
    expect(expressDetail.data.id).toBe(express.data[0].id)
    const expressComments = await request(`/api/express/id/${express.data[0].id}/comment`, { token })
    expect(Array.isArray(expressComments.data)).toBe(true)

    const topic = await request('/api/topic/start/0/size/10', { token })
    expect(topic.data.length).toBeGreaterThan(0)
    const topicDetail = await request(`/api/topic/id/${topic.data[0].id}`, { token })
    expect(topicDetail.data.id).toBe(topic.data[0].id)

    const delivery = await request('/api/delivery/order/start/0/size/10', { token })
    expect(delivery.data.length).toBeGreaterThan(0)
    const deliveryDetail = await request(`/api/delivery/order/id/${delivery.data[0].orderId}`, { token })
    expect(deliveryDetail.data.order.orderId).toBe(delivery.data[0].orderId)
    const deliveryMine = await request('/api/delivery/mine', { token })
    expect(deliveryMine.data).toHaveProperty('published')

    const photoStats = await Promise.all([
      request('/api/photograph/statistics/photos', { token }),
      request('/api/photograph/statistics/comments', { token }),
      request('/api/photograph/statistics/likes', { token }),
    ])
    photoStats.forEach((item) => expect(item.data).toBeGreaterThanOrEqual(0))

    const photographs = await request('/api/photograph/type/0/start/0/size/10', { token })
    expect(photographs.data.length).toBeGreaterThan(0)
    const photographDetail = await request(`/api/photograph/id/${photographs.data[0].id}`, { token })
    expect(photographDetail.data.id).toBe(photographs.data[0].id)
    const photographComments = await request(`/api/photograph/id/${photographs.data[0].id}/comment`, { token })
    expect(Array.isArray(photographComments.data)).toBe(true)
  })
})
