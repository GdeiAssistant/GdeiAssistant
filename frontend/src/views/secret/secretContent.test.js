import { describe, expect, it } from 'vitest'
import {
  createSecretSwitchCopy,
  createSecretVoiceHint,
  getSecretVoiceState
} from './secretContent'

describe('secret localized content', () => {
  const messages = {
    'secret.publish.voiceHintIdle': 'Hold to start recording',
    'secret.publish.voiceHintRecording': 'Recording {duration}',
    'secret.publish.voiceHintRecorded': 'Recorded {duration}',
    'secret.publish.voiceStateIdle': 'No recording',
    'secret.publish.voiceStateFinished': 'Recording ready ({duration})',
    'secret.publish.voiceStateRemaining': 'Recording, {seconds}s left',
    'secret.publish.voiceStateProcessing': 'Processing recording...',
    'secret.publish.voiceStatePreview': 'Previewing recording',
    'secret.publish.switchToTextPrefix': 'Switch to',
    'secret.publish.switchToTextAction': 'text secret',
    'secret.publish.switchToTextSuffix': ', and share it in words',
    'secret.publish.switchToVoicePrefix': 'Switch to',
    'secret.publish.switchToVoiceAction': 'voice secret',
    'secret.publish.switchToVoiceSuffix': ', and share it by voice'
  }

  const t = (key, params = {}) => {
    const template = messages[key] ?? key
    return template.replace(/\{(\w+)\}/g, (_, name) => String(params[name] ?? `{${name}}`))
  }

  it('builds localized recording hints', () => {
    expect(createSecretVoiceHint({ recording: true, hasRecordedAudio: false, duration: '00:12' }, t)).toBe('Recording 00:12')
    expect(createSecretVoiceHint({ recording: false, hasRecordedAudio: true, duration: '00:12' }, t)).toBe('Recorded 00:12')
    expect(createSecretVoiceHint({ recording: false, hasRecordedAudio: false, duration: '00:00' }, t)).toBe('Hold to start recording')
  })

  it('builds localized recording states', () => {
    expect(getSecretVoiceState('finished', t, { duration: '00:20' })).toBe('Recording ready (00:20)')
    expect(getSecretVoiceState('remaining', t, { seconds: 42 })).toBe('Recording, 42s left')
    expect(getSecretVoiceState('processing', t)).toBe('Processing recording...')
  })

  it('builds localized switch copy', () => {
    expect(createSecretSwitchCopy(t)).toEqual({
      text: {
        prefix: 'Switch to',
        action: 'text secret',
        suffix: ', and share it in words'
      },
      voice: {
        prefix: 'Switch to',
        action: 'voice secret',
        suffix: ', and share it by voice'
      }
    })
  })
})
