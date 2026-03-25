import { describe, expect, it } from 'vitest'

import { formatProfileViewModel } from './profileFormatter'

describe('formatProfileViewModel', () => {
  it('formats code-only profile payload into localized display fields', () => {
    const viewModel = formatProfileViewModel({
      username: '20231234',
      nickname: '测试同学',
      avatar: '',
      facultyCode: 11,
      majorCode: 'software_engineering',
      enrollment: '2023',
      location: {
        regionCode: 'CN',
        stateCode: '44',
        cityCode: '1',
      },
      hometown: {
        regionCode: 'CN',
        stateCode: '44',
        cityCode: '5',
      },
      introduction: '喜欢图书馆和校园摄影。',
      ipArea: 'Guangdong Province',
    }, 'en-US')

    expect(viewModel.faculty).toBe('Department of Computer Science')
    expect(viewModel.major).toBe('Software Engineering')
    expect(viewModel.location).toBe('Guangzhou, Guangdong, China')
    expect(viewModel.hometown).toBe('Shantou, Guangdong, China')
    expect(viewModel.locationRegion).toBe('CN')
    expect(viewModel.hometownCity).toBe('5')
  })

  it('falls back to unselected labels when codes are missing', () => {
    const viewModel = formatProfileViewModel({
      username: '20231234',
      facultyCode: null,
      majorCode: '',
      location: null,
      hometown: null,
    }, 'en-US')

    expect(viewModel.faculty).toBe('')
    expect(viewModel.major).toBe('')
    expect(viewModel.location).toBe('')
    expect(viewModel.hometown).toBe('')
  })
})
