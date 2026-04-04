const DELIVERY_DEFAULT_ORDER_NAME = '代收'

const MOCK_IMAGE = {
  avatar: '/img/mock-demo/avatar-assistant.jpg',
  ershou: '/img/mock-demo/marketplace-keyboard.jpg',
  book: '/img/mock-demo/marketplace-books.jpg',
  data: '/img/mock-demo/marketplace-fan.jpg',
  spare: '/img/mock-demo/marketplace-umbrella.jpg',
  lostandfound: '/img/mock-demo/lost-card.jpg',
  card: '/img/mock-demo/lost-id.jpg',
  lostBackpack: '/img/mock-demo/lost-backpack.jpg',
  news: '/img/mock-demo/topic-study.jpg',
  dating: '/img/mock-demo/dating-roommate.jpg',
  datingRunner: '/img/mock-demo/dating-runner.jpg',
  photograph: '/img/mock-demo/photograph-campus.jpg',
  photographCorridor: '/img/mock-demo/photograph-corridor.jpg'
}

const DEFAULT_AVATAR = MOCK_IMAGE.avatar

const COMMUNITY_DEFAULT_STATE = {
  secondhandItems: [
    {
      id: 101,
      name: '九成新机械键盘',
      description: '宿舍自用，青轴，带灯效和原装盒。',
      price: 168,
      location: '海珠校区南苑宿舍楼下',
      type: 2,
      state: 1,
      qq: '214578901',
      phone: '13612340001',
      publishTime: '2026-03-13 20:10',
      pictureURL: [MOCK_IMAGE.ershou],
      owner: 'gdeiassistant'
    },
    {
      id: 102,
      name: '考研数学资料一套',
      description: '张宇基础 36 讲和历年真题，已做少量笔记。',
      price: 48,
      location: '图书馆一楼自助借阅区',
      type: 8,
      state: 1,
      qq: '103454321',
      phone: '13900000001',
      publishTime: '2026-03-14 16:20',
      pictureURL: [MOCK_IMAGE.book],
      owner: 'campus_seller'
    },
    {
      id: 103,
      name: '宿舍小风扇',
      description: 'USB 供电，支持摇头，使用正常。',
      price: 25,
      location: '北苑 8 栋快递柜',
      type: 5,
      state: 0,
      qq: '214578901',
      phone: '13612340001',
      publishTime: '2026-03-10 12:00',
      pictureURL: [MOCK_IMAGE.data],
      owner: 'gdeiassistant'
    },
    {
      id: 104,
      name: '折叠雨伞',
      description: '黑色自动伞，几乎全新。',
      price: 18,
      location: '教学楼 A 栋',
      type: 7,
      state: 2,
      qq: '214578901',
      phone: '13612340001',
      publishTime: '2026-03-09 18:00',
      pictureURL: [MOCK_IMAGE.spare],
      owner: 'gdeiassistant'
    }
  ],
  lostAndFoundItems: [
    {
      id: 201,
      name: '蓝色校园卡',
      description: '卡套上贴有小熊贴纸，卡背写了联系电话。',
      location: '一饭门口',
      lostType: 0,
      itemType: 1,
      state: 0,
      qq: '214578901',
      wechat: 'linzy_2023',
      phone: '13612340001',
      publishTime: '2026-03-15 08:20',
      pictureURL: [MOCK_IMAGE.lostandfound],
      owner: 'gdeiassistant'
    },
    {
      id: 202,
      name: '身份证',
      description: '在图书馆四楼自习区捡到，请失主联系。',
      location: '图书馆四楼',
      lostType: 1,
      itemType: 2,
      state: 0,
      qq: '556677889',
      wechat: '拾金不昧同学',
      phone: '13700002222',
      publishTime: '2026-03-14 19:40',
      pictureURL: [MOCK_IMAGE.card],
      owner: 'library_helper'
    },
    {
      id: 203,
      name: '黑色双肩包',
      description: '内有数据线和一本离散数学笔记。',
      location: 'B 栋 302',
      lostType: 0,
      itemType: 6,
      state: 1,
      qq: '214578901',
      wechat: 'linzy_2023',
      phone: '',
      publishTime: '2026-03-11 09:10',
      pictureURL: [MOCK_IMAGE.lostBackpack],
      owner: 'gdeiassistant'
    }
  ],
  secrets: [
    {
      id: 301,
      content: '今天终于把小程序的 mock 流程跑通了，开心。',
      type: 0,
      theme: 1,
      timer: 0,
      publishTime: '2026-03-15 11:20',
      owner: 'gdeiassistant',
      voiceURL: '',
      likedUsers: ['campus_buddy']
    },
    {
      id: 302,
      content: '最近图书馆四楼位置好难抢，希望早起可以赢一次。',
      type: 0,
      theme: 3,
      timer: 0,
      publishTime: '2026-03-14 22:12',
      owner: 'late_study',
      voiceURL: '',
      likedUsers: ['gdeiassistant']
    }
  ],
  secretComments: {
    301: [
      { id: 1, nickname: '同路人', comment: '继续加油，快发版本！', createTime: '2026-03-15 11:30' }
    ],
    302: [
      { id: 1, nickname: '自习搭子', comment: '早上七点前基本还有位置。', createTime: '2026-03-14 22:18' }
    ]
  },
  expressItems: [
    {
      id: 401,
      nickname: '匿名热心同学',
      realname: '林知远',
      selfGender: 0,
      name: '图书馆靠窗的你',
      personGender: 1,
      content: '你总是在晚自习最后十分钟整理笔记，真的很认真。',
      publishTime: '2026-03-13 20:16',
      owner: 'gdeiassistant',
      likedUsers: ['campus_buddy'],
      guessSum: 2,
      guessCount: 1
    },
    {
      id: 402,
      nickname: '今天也想吃宵夜',
      realname: '',
      selfGender: 1,
      name: '操场夜跑的男生',
      personGender: 0,
      content: '你总穿着蓝色外套，跑步节奏特别稳。',
      publishTime: '2026-03-14 21:30',
      owner: 'campus_crush',
      likedUsers: ['gdeiassistant'],
      guessSum: 0,
      guessCount: 0
    }
  ],
  expressComments: {
    401: [
      { id: 1, nickname: '围观群众', comment: '这也太甜了吧。', publishTime: '2026-03-13 20:26' }
    ],
    402: []
  },
  topics: [
    {
      id: 501,
      topic: '春招实习',
      content: '大家最近投递前端和客户端岗位的反馈怎么样？',
      count: 1,
      imageUrls: [MOCK_IMAGE.news],
      publishTime: '2026-03-15 10:10',
      owner: 'gdeiassistant',
      likedUsers: ['campus_buddy']
    },
    {
      id: 502,
      topic: '图书馆选座',
      content: '自习室哪个楼层最适合背书？',
      count: 0,
      imageUrls: [],
      publishTime: '2026-03-14 18:10',
      owner: 'study_group',
      likedUsers: []
    }
  ],
  deliveryOrders: [
    {
      orderId: 601,
      name: DELIVERY_DEFAULT_ORDER_NAME,
      number: 'A112233',
      phone: '13612340001',
      price: 4,
      company: '菜鸟驿站',
      address: '南苑 5 栋 307',
      remarks: '一个中号纸箱，麻烦轻拿轻放。',
      orderTime: '2026-03-15 12:08',
      state: 0,
      publisher: 'gdeiassistant',
      acceptor: '',
      tradeId: null
    },
    {
      orderId: 602,
      name: DELIVERY_DEFAULT_ORDER_NAME,
      number: 'B009977',
      phone: '13888880002',
      price: 6,
      company: '京东站点',
      address: '北苑 2 栋 411',
      remarks: '已付款，直接放宿舍门口即可。',
      orderTime: '2026-03-14 17:30',
      state: 1,
      publisher: 'campus_runner',
      acceptor: 'gdeiassistant',
      tradeId: 9001
    }
  ],
  datingProfiles: [
    {
      profileId: 701,
      nickname: '阿远的室友',
      grade: 3,
      faculty: '软件工程',
      hometown: '汕头',
      content: '会拍照、会修电脑、食堂很少踩雷。',
      qq: '214578901',
      wechat: 'linzy_2023',
      area: 0,
      pictureURL: MOCK_IMAGE.dating,
      state: 1,
      createTime: '2026-03-12 14:10',
      owner: 'gdeiassistant'
    },
    {
      profileId: 702,
      nickname: '夜跑学长',
      grade: 4,
      faculty: '数学与信息科学学院',
      hometown: '佛山',
      content: '喜欢长跑和拍落日，周末会去图书馆。',
      qq: '334455667',
      wechat: 'runner_senior',
      area: 1,
      pictureURL: MOCK_IMAGE.datingRunner,
      state: 1,
      createTime: '2026-03-11 18:30',
      owner: 'runner_senior'
    }
  ],
  datingPicks: [
    {
      pickId: 801,
      profileId: 701,
      sender: 'campus_buddy',
      content: '可以认识一下吗？感觉你会是很靠谱的搭子。',
      state: 0,
      createTime: '2026-03-14 20:00'
    },
    {
      pickId: 802,
      profileId: 702,
      sender: 'gdeiassistant',
      content: '夜跑结束后有空一起喝奶茶吗？',
      state: 1,
      createTime: '2026-03-14 21:15'
    }
  ],
  photographs: [
    {
      id: 901,
      title: '雨后教学楼',
      content: '下课时刚好遇到一点夕阳。',
      type: 1,
      feedType: 1,
      count: 2,
      imageUrls: [MOCK_IMAGE.photograph, MOCK_IMAGE.photographCorridor],
      createTime: '2026-03-15 17:20',
      owner: 'gdeiassistant',
      likedUsers: ['campus_buddy']
    },
    {
      id: 902,
      title: '图书馆长廊',
      content: '周末的光影很温柔。',
      type: 2,
      feedType: 0,
      count: 1,
      imageUrls: [MOCK_IMAGE.photographCorridor],
      createTime: '2026-03-14 15:45',
      owner: 'photo_club',
      likedUsers: []
    }
  ],
  photographComments: {
    901: [
      { commentId: 1, nickname: '摄影社同学', comment: '构图好舒服。', createTime: '2026-03-15 17:40' }
    ],
    902: []
  }
}

function cloneCommunityState(utils) {
  return utils.cloneValue(COMMUNITY_DEFAULT_STATE)
}

function getCurrentUsername(utils) {
  const state = utils.readState()
  const profile = state && state.profile ? state.profile : {}
  return profile.username || 'gdeiassistant'
}

function ensureCommunityState(utils) {
  const state = utils.readState()
  if (!state.community || typeof state.community !== 'object') {
    state.community = cloneCommunityState(utils)
    utils.writeState(state)
  }
  return state.community
}

function saveCommunityState(utils, communityState) {
  const state = utils.readState()
  state.community = communityState
  utils.writeState(state)
}

function buildProfile(username) {
  return {
    username: username,
    nickname: username === 'gdeiassistant' ? '林知远' : `${username} 同学`,
    avatarURL: DEFAULT_AVATAR,
    introduction: username === 'gdeiassistant' ? '喜欢做实用的小工具。' : '这个人很懒，什么都没写。'
  }
}

function buildSecretPayload(secret, username, commentList) {
  return Object.assign({}, secret, {
    liked: Array.isArray(secret.likedUsers) && secret.likedUsers.indexOf(username) !== -1 ? 1 : 0,
    likeCount: Array.isArray(secret.likedUsers) ? secret.likedUsers.length : 0,
    commentCount: Array.isArray(commentList) ? commentList.length : 0
  })
}

function buildExpressPayload(item, username, commentList) {
  return Object.assign({}, item, {
    liked: Array.isArray(item.likedUsers) && item.likedUsers.indexOf(username) !== -1,
    likeCount: Array.isArray(item.likedUsers) ? item.likedUsers.length : 0,
    commentCount: Array.isArray(commentList) ? commentList.length : 0,
    canGuess: !!item.realname
  })
}

function buildTopicPayload(item, username) {
  return Object.assign({}, item, {
    liked: Array.isArray(item.likedUsers) && item.likedUsers.indexOf(username) !== -1,
    likeCount: Array.isArray(item.likedUsers) ? item.likedUsers.length : 0
  })
}

function buildPhotographPayload(item, username, commentList) {
  return Object.assign({}, item, {
    firstImageUrl: item.imageUrls && item.imageUrls.length ? item.imageUrls[0] : '',
    liked: Array.isArray(item.likedUsers) && item.likedUsers.indexOf(username) !== -1,
    likeCount: Array.isArray(item.likedUsers) ? item.likedUsers.length : 0,
    commentCount: Array.isArray(commentList) ? commentList.length : 0,
    photographCommentList: commentList
  })
}

function findById(list, key, id) {
  const targetId = Number(id)
  return (list || []).filter(function(item) {
    return Number(item[key]) === targetId
  })[0] || null
}

function nextId(list, key, base) {
  const values = (list || []).map(function(item) {
    return Number(item[key]) || 0
  })
  const maxValue = values.length ? Math.max.apply(null, values) : base
  return maxValue + 1
}

function nowText() {
  return '2026-03-15 18:00'
}

function createComment(list, nickname, comment) {
  const nextCommentId = nextId(list, list.length && list[0].commentId !== undefined ? 'commentId' : 'id', 0)
  if (list.length && list[0].commentId !== undefined) {
    return {
      commentId: nextCommentId,
      nickname: nickname,
      comment: comment,
      createTime: nowText()
    }
  }
  return {
    id: nextCommentId,
    nickname: nickname,
    comment: comment,
    publishTime: nowText(),
    createTime: nowText()
  }
}

function getDeliveryDetailType(order, username) {
  if (!order) {
    return 2
  }
  if (order.publisher === username) {
    return 0
  }
  if (!order.acceptor) {
    return 1
  }
  if (order.acceptor === username) {
    return 3
  }
  return 2
}

function buildRoommatePickPayload(pick, communityState) {
  const profile = findById(communityState.datingProfiles, 'profileId', pick.profileId)
  return Object.assign({}, pick, {
    roommateProfile: profile || null
  })
}

function handleSecondhand(path, method, data, token, utils) {
  // 前端视图使用 /api/marketplace/* 路径，mock 内部统一用 /api/ershou/* 处理
  const normalizedPath = path.replace(/^\/api\/marketplace\//, '/api/ershou/')
  if (!/^\/api\/ershou\//.test(normalizedPath)) {
    return null
  }
  path = normalizedPath

  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const communityState = ensureCommunityState(utils)
  const username = getCurrentUsername(utils)

  if (/^\/api\/ershou\/item\/start\/\d+$/.test(path) && method === 'GET') {
    const matched = /\/start\/(\d+)$/.exec(path)
    const start = Number(matched[1])
    const list = communityState.secondhandItems.filter(function(item) {
      return Number(item.state) === 1
    }).slice(start, start + 10)
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/ershou\/keyword\/.+\/start\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/ershou\/keyword\/(.+)\/start\/(\d+)$/.exec(path)
    const keyword = decodeURIComponent(matched[1]).toLowerCase()
    const start = Number(matched[2])
    const list = communityState.secondhandItems.filter(function(item) {
      return Number(item.state) === 1 &&
        (String(item.name).toLowerCase().indexOf(keyword) !== -1 ||
        String(item.description).toLowerCase().indexOf(keyword) !== -1)
    }).slice(start, start + 10)
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/ershou\/item\/type\/\d+\/start\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/ershou\/item\/type\/(\d+)\/start\/(\d+)$/.exec(path)
    const type = Number(matched[1])
    const start = Number(matched[2])
    const list = communityState.secondhandItems.filter(function(item) {
      return Number(item.state) === 1 && Number(item.type) === type
    }).slice(start, start + 10)
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (path === '/api/ershou/profile' && method === 'GET') {
    const mine = communityState.secondhandItems.filter(function(item) {
      return item.owner === username
    })
    return utils.resolveWithDelay(utils.buildSuccess({
      doing: mine.filter(function(item) { return Number(item.state) === 1 }),
      sold: mine.filter(function(item) { return Number(item.state) === 0 }),
      off: mine.filter(function(item) { return Number(item.state) === 2 })
    }))
  }

  if (/^\/api\/ershou\/item\/id\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/ershou\/item\/id\/(\d+)$/.exec(path)
    const item = findById(communityState.secondhandItems, 'id', matched[1])
    if (!item) {
      return utils.rejectWithMessage('商品不存在')
    }
    return utils.resolveWithDelay(utils.buildSuccess({
      secondhandItem: item,
      profile: buildProfile(item.owner)
    }))
  }

  if (path === '/api/ershou/item' && method === 'POST') {
    const nextItem = {
      id: nextId(communityState.secondhandItems, 'id', 100),
      name: String(data.name || '').trim(),
      description: String(data.description || '').trim(),
      price: Number(data.price || 0),
      location: String(data.location || '').trim(),
      type: Number(data.type || 0),
      state: 1,
      qq: String(data.qq || '').trim(),
      phone: String(data.phone || '').trim(),
      publishTime: nowText(),
      pictureURL: Array.isArray(data.imageKeys) ? data.imageKeys : [data.imageKeys].filter(Boolean),
      owner: username
    }
    if (!nextItem.name || !nextItem.description || !nextItem.location || !nextItem.pictureURL.length) {
      return utils.rejectWithMessage('请完整填写商品信息')
    }
    if (!(nextItem.price > 0)) {
      return utils.rejectWithMessage('请输入正确的商品价格')
    }
    communityState.secondhandItems.unshift(nextItem)
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/ershou\/item\/id\/\d+$/.test(path) && method === 'POST') {
    const matched = /^\/api\/ershou\/item\/id\/(\d+)$/.exec(path)
    const item = findById(communityState.secondhandItems, 'id', matched[1])
    if (!item || item.owner !== username) {
      return utils.rejectWithMessage('没有权限编辑该商品')
    }

    item.name = String(data.name || '').trim()
    item.description = String(data.description || '').trim()
    item.price = Number(data.price || 0)
    item.location = String(data.location || '').trim()
    item.type = Number(data.type || 0)
    item.qq = String(data.qq || '').trim()
    item.phone = String(data.phone || '').trim()

    if (!item.name || !item.description || !item.location) {
      return utils.rejectWithMessage('请完整填写商品信息')
    }
    if (!(item.price > 0)) {
      return utils.rejectWithMessage('请输入正确的商品价格')
    }

    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/ershou\/item\/state\/id\/\d+$/.test(path) && method === 'POST') {
    const matched = /^\/api\/ershou\/item\/state\/id\/(\d+)$/.exec(path)
    const item = findById(communityState.secondhandItems, 'id', matched[1])
    if (!item || item.owner !== username) {
      return utils.rejectWithMessage('没有权限操作该商品')
    }
    item.state = Number(data.state || 0)
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  return null
}

function handleLostAndFound(path, method, data, token, utils) {
  if (!/^\/api\/lostandfound\//.test(path)) {
    return null
  }

  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const communityState = ensureCommunityState(utils)
  const username = getCurrentUsername(utils)

  if (/^\/api\/lostandfound\/lostitem\/start\/\d+$/.test(path) && method === 'GET') {
    const start = Number(/\/start\/(\d+)$/.exec(path)[1])
    return utils.resolveWithDelay(utils.buildSuccess(communityState.lostAndFoundItems.filter(function(item) {
      return Number(item.state) === 0 && Number(item.lostType) === 0
    }).slice(start, start + 10)))
  }

  if (/^\/api\/lostandfound\/founditem\/start\/\d+$/.test(path) && method === 'GET') {
    const start = Number(/\/start\/(\d+)$/.exec(path)[1])
    return utils.resolveWithDelay(utils.buildSuccess(communityState.lostAndFoundItems.filter(function(item) {
      return Number(item.state) === 0 && Number(item.lostType) === 1
    }).slice(start, start + 10)))
  }

  if (/^\/api\/lostandfound\/lostitem\/type\/\d+\/start\/\d+$/.test(path) && method === 'POST') {
    const matched = /^\/api\/lostandfound\/lostitem\/type\/(\d+)\/start\/(\d+)$/.exec(path)
    const lostType = Number(matched[1])
    const start = Number(matched[2])
    const keyword = String(data.keyword || '').trim().toLowerCase()
    const list = communityState.lostAndFoundItems.filter(function(item) {
      return Number(item.state) === 0 &&
        Number(item.lostType) === lostType &&
        (String(item.name).toLowerCase().indexOf(keyword) !== -1 ||
        String(item.description).toLowerCase().indexOf(keyword) !== -1)
    }).slice(start, start + 10)
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (path === '/api/lostandfound/profile' && method === 'GET') {
    const mine = communityState.lostAndFoundItems.filter(function(item) {
      return item.owner === username
    })
    return utils.resolveWithDelay(utils.buildSuccess({
      lost: mine.filter(function(item) { return Number(item.state) === 0 && Number(item.lostType) === 0 }),
      found: mine.filter(function(item) { return Number(item.state) === 0 && Number(item.lostType) === 1 }),
      didfound: mine.filter(function(item) { return Number(item.state) === 1 })
    }))
  }

  if (/^\/api\/lostandfound\/item\/id\/\d+$/.test(path) && method === 'GET') {
    const item = findById(communityState.lostAndFoundItems, 'id', /^\/api\/lostandfound\/item\/id\/(\d+)$/.exec(path)[1])
    if (!item) {
      return utils.rejectWithMessage('信息不存在')
    }
    return utils.resolveWithDelay(utils.buildSuccess({
      item: item,
      profile: buildProfile(item.owner)
    }))
  }

  if (path === '/api/lostandfound/item' && method === 'POST') {
    const nextItem = {
      id: nextId(communityState.lostAndFoundItems, 'id', 200),
      name: String(data.name || '').trim(),
      description: String(data.description || '').trim(),
      location: String(data.location || '').trim(),
      lostType: Number(data.lostType || 0),
      itemType: Number(data.itemType || 0),
      state: 0,
      qq: String(data.qq || '').trim(),
      wechat: String(data.wechat || '').trim(),
      phone: String(data.phone || '').trim(),
      publishTime: nowText(),
      pictureURL: Array.isArray(data.imageKeys) ? data.imageKeys : [data.imageKeys].filter(Boolean),
      owner: username
    }
    if (!nextItem.name || !nextItem.description || !nextItem.location || !nextItem.pictureURL.length) {
      return utils.rejectWithMessage('请完整填写失物信息')
    }
    communityState.lostAndFoundItems.unshift(nextItem)
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/lostandfound\/item\/id\/\d+$/.test(path) && method === 'POST') {
    const matched = /^\/api\/lostandfound\/item\/id\/(\d+)$/.exec(path)
    const item = findById(communityState.lostAndFoundItems, 'id', matched[1])
    if (!item || item.owner !== username) {
      return utils.rejectWithMessage('没有权限编辑该信息')
    }

    item.name = String(data.name || '').trim()
    item.description = String(data.description || '').trim()
    item.location = String(data.location || '').trim()
    item.lostType = Number(data.lostType || 0)
    item.itemType = Number(data.itemType || 0)
    item.qq = String(data.qq || '').trim()
    item.wechat = String(data.wechat || '').trim()
    item.phone = String(data.phone || '').trim()

    if (!item.name || !item.description || !item.location) {
      return utils.rejectWithMessage('请完整填写失物信息')
    }
    if (!item.qq && !item.wechat && !item.phone) {
      return utils.rejectWithMessage('请至少填写一种联系方式')
    }

    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/lostandfound\/item\/id\/\d+\/didfound$/.test(path) && method === 'POST') {
    const item = findById(communityState.lostAndFoundItems, 'id', /^\/api\/lostandfound\/item\/id\/(\d+)\/didfound$/.exec(path)[1])
    if (!item || item.owner !== username) {
      return utils.rejectWithMessage('没有权限操作该信息')
    }
    item.state = 1
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  return null
}

function handleSecret(path, method, data, token, utils) {
  if (!/^\/api\/secret\//.test(path)) {
    return null
  }

  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const communityState = ensureCommunityState(utils)
  const username = getCurrentUsername(utils)

  if (/^\/api\/secret\/info\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/secret\/info\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const start = Number(matched[1])
    const size = Number(matched[2])
    const list = communityState.secrets.slice(start, start + size).map(function(item) {
      return buildSecretPayload(item, username, communityState.secretComments[item.id] || [])
    })
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/secret\/profile(\/start\/\d+\/size\/\d+)?$/.test(path) && method === 'GET') {
    const pageMatch = /\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const start = pageMatch ? Number(pageMatch[1]) : 0
    const size = pageMatch ? Number(pageMatch[2]) : 20
    const list = communityState.secrets.filter(function(item) {
      return item.owner === username
    }).map(function(item) {
      return buildSecretPayload(item, username, communityState.secretComments[item.id] || [])
    }).slice(start, start + size)
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/secret\/id\/\d+$/.test(path) && method === 'GET') {
    const secretId = /^\/api\/secret\/id\/(\d+)$/.exec(path)[1]
    const item = findById(communityState.secrets, 'id', secretId)
    if (!item) {
      return utils.rejectWithMessage('树洞不存在')
    }
    return utils.resolveWithDelay(utils.buildSuccess(buildSecretPayload(item, username, communityState.secretComments[item.id] || [])))
  }

  if (/^\/api\/secret\/id\/\d+\/comments$/.test(path) && method === 'GET') {
    const secretId = Number(/^\/api\/secret\/id\/(\d+)\/comments$/.exec(path)[1])
    return utils.resolveWithDelay(utils.buildSuccess((communityState.secretComments[secretId] || []).slice()))
  }

  if (/^\/api\/secret\/id\/\d+\/comment$/.test(path) && method === 'POST') {
    const secretId = Number(/^\/api\/secret\/id\/(\d+)\/comment$/.exec(path)[1])
    const item = findById(communityState.secrets, 'id', secretId)
    if (!item) {
      return utils.rejectWithMessage('树洞不存在')
    }
    const comment = String(data.comment || '').trim()
    if (!comment) {
      return utils.rejectWithMessage('评论不能为空')
    }
    const list = communityState.secretComments[secretId] || []
    list.push(createComment(list, '我', comment))
    communityState.secretComments[secretId] = list
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/secret\/id\/\d+\/like$/.test(path) && method === 'POST') {
    const secretId = Number(/^\/api\/secret\/id\/(\d+)\/like$/.exec(path)[1])
    const item = findById(communityState.secrets, 'id', secretId)
    if (!item) {
      return utils.rejectWithMessage('树洞不存在')
    }
    const like = Number(data.like || 0)
    item.likedUsers = Array.isArray(item.likedUsers) ? item.likedUsers : []
    if (like === 1 && item.likedUsers.indexOf(username) === -1) {
      item.likedUsers.push(username)
    }
    if (like === 0) {
      item.likedUsers = item.likedUsers.filter(function(userItem) {
        return userItem !== username
      })
    }
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (path === '/api/secret/info' && method === 'POST') {
    const type = Number(data.type || 0)
    const content = String(data.content || '').trim()
    const voiceKey = String(data.voiceKey || '').trim()
    if (type === 0 && !content) {
      return utils.rejectWithMessage('树洞内容不能为空')
    }
    if (type === 1 && !voiceKey) {
      return utils.rejectWithMessage('语音内容不能为空')
    }
    const nextSecret = {
      id: nextId(communityState.secrets, 'id', 300),
      content: content || '语音树洞',
      type: type,
      theme: Number(data.theme || 1),
      timer: Number(data.timer || 0),
      publishTime: nowText(),
      owner: username,
      voiceURL: voiceKey,
      likedUsers: []
    }
    communityState.secrets.unshift(nextSecret)
    communityState.secretComments[nextSecret.id] = []
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  return null
}

function handleExpress(path, method, data, token, utils) {
  if (!/^\/api\/express\//.test(path)) {
    return null
  }

  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const communityState = ensureCommunityState(utils)
  const username = getCurrentUsername(utils)

  if (/^\/api\/express\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/express\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const start = Number(matched[1])
    const size = Number(matched[2])
    const list = communityState.expressItems.slice(start, start + size).map(function(item) {
      return buildExpressPayload(item, username, communityState.expressComments[item.id] || [])
    })
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/express\/keyword\/.+\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/express\/keyword\/(.+)\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const keyword = decodeURIComponent(matched[1]).toLowerCase()
    const start = Number(matched[2])
    const size = Number(matched[3])
    const list = communityState.expressItems.filter(function(item) {
      return String(item.nickname).toLowerCase().indexOf(keyword) !== -1 ||
        String(item.name).toLowerCase().indexOf(keyword) !== -1 ||
        String(item.content).toLowerCase().indexOf(keyword) !== -1
    }).slice(start, start + size).map(function(item) {
      return buildExpressPayload(item, username, communityState.expressComments[item.id] || [])
    })
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/express\/profile\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/express\/profile\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const start = Number(matched[1])
    const size = Number(matched[2])
    const list = communityState.expressItems.filter(function(item) {
      return item.owner === username
    }).slice(start, start + size).map(function(item) {
      return buildExpressPayload(item, username, communityState.expressComments[item.id] || [])
    })
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/express\/id\/\d+$/.test(path) && method === 'GET') {
    const item = findById(communityState.expressItems, 'id', /^\/api\/express\/id\/(\d+)$/.exec(path)[1])
    if (!item) {
      return utils.rejectWithMessage('表白信息不存在')
    }
    return utils.resolveWithDelay(utils.buildSuccess(buildExpressPayload(item, username, communityState.expressComments[item.id] || [])))
  }

  if (/^\/api\/express\/id\/\d+\/comment$/.test(path) && method === 'GET') {
    const expressId = Number(/^\/api\/express\/id\/(\d+)\/comment$/.exec(path)[1])
    return utils.resolveWithDelay(utils.buildSuccess((communityState.expressComments[expressId] || []).slice()))
  }

  if (/^\/api\/express\/id\/\d+\/comment$/.test(path) && method === 'POST') {
    const expressId = Number(/^\/api\/express\/id\/(\d+)\/comment$/.exec(path)[1])
    const item = findById(communityState.expressItems, 'id', expressId)
    const comment = String(data.comment || '').trim()
    if (!item || !comment) {
      return utils.rejectWithMessage('评论不能为空')
    }
    const list = communityState.expressComments[expressId] || []
    list.push(createComment(list, '我', comment))
    communityState.expressComments[expressId] = list
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/express\/id\/\d+\/like$/.test(path) && method === 'POST') {
    const item = findById(communityState.expressItems, 'id', /^\/api\/express\/id\/(\d+)\/like$/.exec(path)[1])
    if (!item) {
      return utils.rejectWithMessage('表白信息不存在')
    }
    item.likedUsers = Array.isArray(item.likedUsers) ? item.likedUsers : []
    if (item.likedUsers.indexOf(username) === -1) {
      item.likedUsers.push(username)
      saveCommunityState(utils, communityState)
    }
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/express\/id\/\d+\/guess$/.test(path) && method === 'POST') {
    const item = findById(communityState.expressItems, 'id', /^\/api\/express\/id\/(\d+)\/guess$/.exec(path)[1])
    const guessedName = String(data.name || '').trim().toLowerCase()
    if (!item || !item.realname) {
      return utils.rejectWithMessage('该表白不支持猜名字')
    }
    item.guessSum = Number(item.guessSum || 0) + 1
    const correct = String(item.realname || '').trim().toLowerCase() === guessedName
    if (correct) {
      item.guessCount = Number(item.guessCount || 0) + 1
    }
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(correct))
  }

  if (path === '/api/express' && method === 'POST') {
    const nextItem = {
      id: nextId(communityState.expressItems, 'id', 400),
      nickname: String(data.nickname || '').trim(),
      realname: String(data.realname || '').trim(),
      selfGender: Number(data.selfGender || 0),
      name: String(data.name || '').trim(),
      personGender: Number(data.personGender || 0),
      content: String(data.content || '').trim(),
      publishTime: nowText(),
      owner: username,
      likedUsers: [],
      guessSum: 0,
      guessCount: 0
    }
    if (!nextItem.nickname || !nextItem.name || !nextItem.content) {
      return utils.rejectWithMessage('请完整填写表白信息')
    }
    communityState.expressItems.unshift(nextItem)
    communityState.expressComments[nextItem.id] = []
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  return null
}

function handleTopic(path, method, data, token, utils) {
  if (!/^\/api\/topic\//.test(path)) {
    return null
  }

  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const communityState = ensureCommunityState(utils)
  const username = getCurrentUsername(utils)

  if (/^\/api\/topic\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/topic\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const start = Number(matched[1])
    const size = Number(matched[2])
    return utils.resolveWithDelay(utils.buildSuccess(communityState.topics.slice(start, start + size).map(function(item) {
      return buildTopicPayload(item, username)
    })))
  }

  if (/^\/api\/topic\/keyword\/.+\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/topic\/keyword\/(.+)\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const keyword = decodeURIComponent(matched[1]).toLowerCase()
    const start = Number(matched[2])
    const size = Number(matched[3])
    const list = communityState.topics.filter(function(item) {
      return String(item.topic).toLowerCase().indexOf(keyword) !== -1 ||
        String(item.content).toLowerCase().indexOf(keyword) !== -1
    }).slice(start, start + size).map(function(item) {
      return buildTopicPayload(item, username)
    })
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/topic\/profile\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/topic\/profile\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const start = Number(matched[1])
    const size = Number(matched[2])
    const list = communityState.topics.filter(function(item) {
      return item.owner === username
    }).slice(start, start + size).map(function(item) {
      return buildTopicPayload(item, username)
    })
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/topic\/id\/\d+$/.test(path) && method === 'GET') {
    const item = findById(communityState.topics, 'id', /^\/api\/topic\/id\/(\d+)$/.exec(path)[1])
    if (!item) {
      return utils.rejectWithMessage('话题不存在')
    }
    return utils.resolveWithDelay(utils.buildSuccess(buildTopicPayload(item, username)))
  }

  if (/^\/api\/topic\/id\/\d+\/like$/.test(path) && method === 'POST') {
    const item = findById(communityState.topics, 'id', /^\/api\/topic\/id\/(\d+)\/like$/.exec(path)[1])
    if (!item) {
      return utils.rejectWithMessage('话题不存在')
    }
    item.likedUsers = Array.isArray(item.likedUsers) ? item.likedUsers : []
    if (item.likedUsers.indexOf(username) === -1) {
      item.likedUsers.push(username)
      saveCommunityState(utils, communityState)
    }
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (path === '/api/topic' && method === 'POST') {
    const imageUrls = Array.isArray(data.imageKeys) ? data.imageKeys : [data.imageKeys].filter(Boolean)
    const nextItem = {
      id: nextId(communityState.topics, 'id', 500),
      topic: String(data.topic || '').trim(),
      content: String(data.content || '').trim(),
      count: imageUrls.length,
      imageUrls: imageUrls,
      publishTime: nowText(),
      owner: username,
      likedUsers: []
    }
    if (!nextItem.topic || !nextItem.content) {
      return utils.rejectWithMessage('请完整填写话题信息')
    }
    communityState.topics.unshift(nextItem)
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  return null
}

function handleDelivery(path, method, data, token, utils) {
  if (!/^\/api\/delivery\//.test(path)) {
    return null
  }

  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const communityState = ensureCommunityState(utils)
  const username = getCurrentUsername(utils)

  if (/^\/api\/delivery\/order\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/delivery\/order\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const start = Number(matched[1])
    const size = Number(matched[2])
    return utils.resolveWithDelay(utils.buildSuccess(communityState.deliveryOrders.slice(start, start + size)))
  }

  if (/^\/api\/delivery\/order\/id\/\d+$/.test(path) && method === 'GET') {
    const order = findById(communityState.deliveryOrders, 'orderId', /^\/api\/delivery\/order\/id\/(\d+)$/.exec(path)[1])
    if (!order) {
      return utils.rejectWithMessage('订单不存在')
    }
    return utils.resolveWithDelay(utils.buildSuccess({
      order: order,
      detailType: getDeliveryDetailType(order, username),
      trade: order.tradeId ? {
        tradeId: order.tradeId,
        orderId: order.orderId,
        acceptor: order.acceptor,
        publisher: order.publisher
      } : null
    }))
  }

  if (path === '/api/delivery/mine' && method === 'GET') {
    return utils.resolveWithDelay(utils.buildSuccess({
      published: communityState.deliveryOrders.filter(function(item) {
        return item.publisher === username
      }),
      accepted: communityState.deliveryOrders.filter(function(item) {
        return item.acceptor === username
      })
    }))
  }

  if (path === '/api/delivery/acceptorder' && method === 'POST') {
    const order = findById(communityState.deliveryOrders, 'orderId', data.orderId)
    if (!order) {
      return utils.rejectWithMessage('订单不存在')
    }
    if (order.publisher === username) {
      return utils.rejectWithMessage('不能接自己发布的订单')
    }
    if (Number(order.state) !== 0) {
      return utils.rejectWithMessage('订单已被接取')
    }
    order.state = 1
    order.acceptor = username
    order.tradeId = nextId(communityState.deliveryOrders, 'tradeId', 9000)
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/delivery\/trade\/id\/\d+\/finishtrade$/.test(path) && method === 'POST') {
    const tradeId = Number(/^\/api\/delivery\/trade\/id\/(\d+)\/finishtrade$/.exec(path)[1])
    const order = communityState.deliveryOrders.filter(function(item) {
      return Number(item.tradeId) === tradeId
    })[0]
    if (!order) {
      return utils.rejectWithMessage('交易不存在')
    }
    if (order.publisher !== username) {
      return utils.rejectWithMessage('只有发布者可确认完成')
    }
    order.state = 2
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (path === '/api/delivery/order' && method === 'POST') {
    const nextOrder = {
      orderId: nextId(communityState.deliveryOrders, 'orderId', 600),
      name: String(data.name || DELIVERY_DEFAULT_ORDER_NAME).trim(),
      number: String(data.number || '').trim(),
      phone: String(data.phone || '').trim(),
      price: Number(data.price || 0),
      company: String(data.company || '').trim(),
      address: String(data.address || '').trim(),
      remarks: String(data.remarks || '').trim(),
      orderTime: nowText(),
      state: 0,
      publisher: username,
      acceptor: '',
      tradeId: null
    }
    if (!nextOrder.phone || !nextOrder.company || !nextOrder.address || !(nextOrder.price > 0)) {
      return utils.rejectWithMessage('请完整填写跑腿订单信息')
    }
    communityState.deliveryOrders.unshift(nextOrder)
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  return null
}

function handleDating(path, method, data, token, utils) {
  if (!/^\/api\/dating\//.test(path)) {
    return null
  }

  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const communityState = ensureCommunityState(utils)
  const username = getCurrentUsername(utils)

  if (/^\/api\/dating\/profile\/area\/\d+\/start\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/dating\/profile\/area\/(\d+)\/start\/(\d+)$/.exec(path)
    const area = Number(matched[1])
    const start = Number(matched[2])
    const list = communityState.datingProfiles.filter(function(item) {
      return Number(item.area) === area && Number(item.state) !== 0
    }).slice(start, start + 10)
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/dating\/profile\/id\/\d+$/.test(path) && method === 'GET') {
    const profile = findById(communityState.datingProfiles, 'profileId', /^\/api\/dating\/profile\/id\/(\d+)$/.exec(path)[1])
    if (!profile) {
      return utils.rejectWithMessage('信息不存在')
    }
    const relatedPick = communityState.datingPicks.filter(function(item) {
      return Number(item.profileId) === Number(profile.profileId) && item.sender === username && Number(item.state) === 1
    })[0]
    return utils.resolveWithDelay(utils.buildSuccess({
      profile: profile,
      pictureURL: profile.pictureURL,
      isContactVisible: profile.owner === username || !!relatedPick,
      isPickNotAvailable: !!relatedPick
    }))
  }

  if (path === '/api/dating/profile/my' && method === 'GET') {
    return utils.resolveWithDelay(utils.buildSuccess(communityState.datingProfiles.filter(function(item) {
      return item.owner === username
    })))
  }

  if (path === '/api/dating/pick/my/sent' && method === 'GET') {
    return utils.resolveWithDelay(utils.buildSuccess(communityState.datingPicks.filter(function(item) {
      return item.sender === username
    }).map(function(item) {
      return buildRoommatePickPayload(item, communityState)
    })))
  }

  if (path === '/api/dating/pick/my/received' && method === 'GET') {
    return utils.resolveWithDelay(utils.buildSuccess(communityState.datingPicks.filter(function(item) {
      const profile = findById(communityState.datingProfiles, 'profileId', item.profileId)
      return profile && profile.owner === username
    }).map(function(item) {
      return buildRoommatePickPayload(item, communityState)
    })))
  }

  if (path === '/api/dating/profile' && method === 'POST') {
    const nextProfile = {
      profileId: nextId(communityState.datingProfiles, 'profileId', 700),
      nickname: String(data.nickname || '').trim(),
      grade: Number(data.grade || 1),
      faculty: String(data.faculty || '').trim(),
      hometown: String(data.hometown || '').trim(),
      content: String(data.content || '').trim(),
      qq: String(data.qq || '').trim(),
      wechat: String(data.wechat || '').trim(),
      area: Number(data.area || 0),
      pictureURL: String(data.imageKey || MOCK_IMAGE.dating),
      state: 1,
      createTime: nowText(),
      owner: username
    }
    if (!nextProfile.nickname || !nextProfile.faculty || !nextProfile.hometown || !nextProfile.content) {
      return utils.rejectWithMessage('请完整填写交友信息')
    }
    communityState.datingProfiles.unshift(nextProfile)
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (path === '/api/dating/pick' && method === 'POST') {
    const profileId = Number(data.profileId || 0)
    const profile = findById(communityState.datingProfiles, 'profileId', profileId)
    if (!profile) {
      return utils.rejectWithMessage('目标信息不存在')
    }
    if (profile.owner === username) {
      return utils.rejectWithMessage('不能给自己发送撩一下')
    }
    const exists = communityState.datingPicks.filter(function(item) {
      return Number(item.profileId) === profileId && item.sender === username
    })[0]
    if (exists) {
      return utils.rejectWithMessage('你已经发送过撩一下了')
    }
    communityState.datingPicks.unshift({
      pickId: nextId(communityState.datingPicks, 'pickId', 800),
      profileId: profileId,
      sender: username,
      content: String(data.content || '').trim(),
      state: 0,
      createTime: nowText()
    })
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/dating\/pick\/id\/\d+$/.test(path) && method === 'POST') {
    const pick = findById(communityState.datingPicks, 'pickId', /^\/api\/dating\/pick\/id\/(\d+)$/.exec(path)[1])
    if (!pick) {
      return utils.rejectWithMessage('请求不存在')
    }
    const profile = findById(communityState.datingProfiles, 'profileId', pick.profileId)
    if (!profile || profile.owner !== username) {
      return utils.rejectWithMessage('没有权限操作该请求')
    }
    pick.state = Number(data.state || -1)
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/dating\/profile\/id\/\d+\/state$/.test(path) && method === 'POST') {
    const profile = findById(communityState.datingProfiles, 'profileId', /^\/api\/dating\/profile\/id\/(\d+)\/state$/.exec(path)[1])
    if (!profile || profile.owner !== username) {
      return utils.rejectWithMessage('没有权限操作该信息')
    }
    profile.state = Number(data.state || 0)
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  return null
}

function handlePhotograph(path, method, data, token, utils) {
  if (!/^\/api\/photograph\//.test(path)) {
    return null
  }

  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const communityState = ensureCommunityState(utils)
  const username = getCurrentUsername(utils)

  if (path === '/api/photograph/statistics/photos' && method === 'GET') {
    return utils.resolveWithDelay(utils.buildSuccess(communityState.photographs.length))
  }
  if (path === '/api/photograph/statistics/comments' && method === 'GET') {
    const commentCount = Object.keys(communityState.photographComments).reduce(function(total, key) {
      return total + (communityState.photographComments[key] || []).length
    }, 0)
    return utils.resolveWithDelay(utils.buildSuccess(commentCount))
  }
  if (path === '/api/photograph/statistics/likes' && method === 'GET') {
    const likeCount = communityState.photographs.reduce(function(total, item) {
      return total + (Array.isArray(item.likedUsers) ? item.likedUsers.length : 0)
    }, 0)
    return utils.resolveWithDelay(utils.buildSuccess(likeCount))
  }

  if (/^\/api\/photograph\/type\/\d+\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/photograph\/type\/(\d+)\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const type = Number(matched[1])
    const start = Number(matched[2])
    const size = Number(matched[3])
    const list = communityState.photographs.filter(function(item) {
      return Number(item.feedType) === type
    }).slice(start, start + size).map(function(item) {
      return buildPhotographPayload(item, username, communityState.photographComments[item.id] || [])
    })
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (/^\/api\/photograph\/id\/\d+$/.test(path) && method === 'GET') {
    const item = findById(communityState.photographs, 'id', /^\/api\/photograph\/id\/(\d+)$/.exec(path)[1])
    if (!item) {
      return utils.rejectWithMessage('作品不存在')
    }
    return utils.resolveWithDelay(utils.buildSuccess(buildPhotographPayload(item, username, communityState.photographComments[item.id] || [])))
  }

  if (/^\/api\/photograph\/id\/\d+\/comment$/.test(path) && method === 'GET') {
    const photoId = Number(/^\/api\/photograph\/id\/(\d+)\/comment$/.exec(path)[1])
    return utils.resolveWithDelay(utils.buildSuccess((communityState.photographComments[photoId] || []).slice()))
  }

  if (/^\/api\/photograph\/id\/\d+\/comment$/.test(path) && method === 'POST') {
    const photoId = Number(/^\/api\/photograph\/id\/(\d+)\/comment$/.exec(path)[1])
    const item = findById(communityState.photographs, 'id', photoId)
    const comment = String(data.comment || '').trim()
    if (!item || !comment) {
      return utils.rejectWithMessage('评论不能为空')
    }
    const list = communityState.photographComments[photoId] || []
    list.push(createComment(list, '我', comment))
    communityState.photographComments[photoId] = list
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/photograph\/profile\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const matched = /^\/api\/photograph\/profile\/start\/(\d+)\/size\/(\d+)$/.exec(path)
    const start = Number(matched[1])
    const size = Number(matched[2])
    const list = communityState.photographs.filter(function(item) {
      return item.owner === username
    }).slice(start, start + size).map(function(item) {
      return buildPhotographPayload(item, username, communityState.photographComments[item.id] || [])
    })
    return utils.resolveWithDelay(utils.buildSuccess(list))
  }

  if (path === '/api/photograph' && method === 'POST') {
    const imageUrls = Array.isArray(data.imageKeys) ? data.imageKeys : [data.imageKeys].filter(Boolean)
    const nextItem = {
      id: nextId(communityState.photographs, 'id', 900),
      title: String(data.title || '').trim(),
      content: String(data.content || '').trim(),
      type: Number(data.type || 1),
      feedType: Number(data.type || 1) === 2 ? 0 : 1,
      count: imageUrls.length,
      imageUrls: imageUrls,
      createTime: nowText(),
      owner: username,
      likedUsers: []
    }
    if (!nextItem.title || !nextItem.imageUrls.length) {
      return utils.rejectWithMessage('请至少上传一张图片')
    }
    communityState.photographs.unshift(nextItem)
    communityState.photographComments[nextItem.id] = []
    saveCommunityState(utils, communityState)
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  if (/^\/api\/photograph\/id\/\d+\/like$/.test(path) && method === 'POST') {
    const item = findById(communityState.photographs, 'id', /^\/api\/photograph\/id\/(\d+)\/like$/.exec(path)[1])
    if (!item) {
      return utils.rejectWithMessage('作品不存在')
    }
    item.likedUsers = Array.isArray(item.likedUsers) ? item.likedUsers : []
    if (item.likedUsers.indexOf(username) === -1) {
      item.likedUsers.push(username)
      saveCommunityState(utils, communityState)
    }
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  return null
}

export function handleRequest(options) {
  const requestOptions = options || {}
  const path = requestOptions.path || ''
  const method = String(requestOptions.method || 'GET').toUpperCase()
  const payload = Object.assign({}, requestOptions.payload || {}, requestOptions.query || {})
  const token = requestOptions.token || ''
  const utils = requestOptions.utils || {}

  return handleSecondhand(path, method, payload, token, utils) ||
    handleLostAndFound(path, method, payload, token, utils) ||
    handleSecret(path, method, payload, token, utils) ||
    handleExpress(path, method, payload, token, utils) ||
    handleTopic(path, method, payload, token, utils) ||
    handleDelivery(path, method, payload, token, utils) ||
    handleDating(path, method, payload, token, utils) ||
    handlePhotograph(path, method, payload, token, utils)
}
