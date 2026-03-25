import { describe, expect, it } from 'vitest'

import {
  handleLocationList,
  handleLocationUpdate,
  handleProfile,
  handleProfileOptions,
} from './profile-handlers'
import { BASE_PROFILE } from './mock-data'

function createUtils(profile = BASE_PROFILE) {
  const state = {
    profile: JSON.parse(JSON.stringify(profile)),
  }

  return {
    cloneValue(value) {
      return JSON.parse(JSON.stringify(value))
    },
    readState() {
      return state
    },
    writeState(nextState) {
      state.profile = JSON.parse(JSON.stringify(nextState.profile))
    },
    buildSuccess(data) {
      return {
        success: true,
        code: 200,
        data,
      }
    },
    resolveWithDelay(payload) {
      return Promise.resolve(payload)
    },
    rejectWithMessage(message) {
      return Promise.reject(new Error(message))
    },
    ensureAuthorized() {
      return null
    },
  }
}

describe('profile-handlers', () => {
  it('returns code-only profile payload', async () => {
    const utils = createUtils()

    const response = await handleProfile('token', utils)

    expect(response.data.facultyCode).toBe(11)
    expect(response.data.majorCode).toBe('software_engineering')
    expect(response.data.location).toEqual({
      regionCode: 'CN',
      stateCode: '44',
      cityCode: '1',
    })
    expect(response.data).not.toHaveProperty('faculty')
    expect(response.data).not.toHaveProperty('major')
    expect(response.data).not.toHaveProperty('locationRegion')
  })

  it('returns code-only profile options payload', async () => {
    const utils = createUtils()

    const response = await handleProfileOptions('token', utils)

    expect(response.data.faculties[0]).toEqual({
      code: 1,
      majors: expect.arrayContaining(['education']),
    })
    expect(response.data.marketplaceItemTypes[0]).toBe(0)
    expect(response.data.lostFoundModes).toEqual([0, 1])
  })

  it('returns code-only location tree', async () => {
    const utils = createUtils()

    const response = await handleLocationList('token', utils)

    expect(response.data[0]).toEqual({
      code: expect.any(String),
      children: expect.any(Array),
    })
    expect(response.data[0]).not.toHaveProperty('name')
    expect(response.data[0].children[0]).not.toHaveProperty('name')
  })

  it('stores structured location payload on update', async () => {
    const utils = createUtils()

    await handleLocationUpdate('token', {
      region: 'CN',
      state: '44',
      city: '5',
    }, 'hometown', utils)

    expect(utils.readState().profile.hometown).toEqual({
      regionCode: 'CN',
      stateCode: '44',
      cityCode: '5',
    })
    expect(utils.readState().profile).not.toHaveProperty('hometownRegion')
    expect(utils.readState().profile).not.toHaveProperty('hometownCity')
  })
})
