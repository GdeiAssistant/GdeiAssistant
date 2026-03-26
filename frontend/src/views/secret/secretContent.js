export function createSecretVoiceHint({ recording, hasRecordedAudio, duration }, t) {
  if (recording) {
    return t('secret.publish.voiceHintRecording', { duration })
  }

  if (hasRecordedAudio) {
    return t('secret.publish.voiceHintRecorded', { duration })
  }

  return t('secret.publish.voiceHintIdle')
}

export function getSecretVoiceState(state, t, params = {}) {
  if (state === 'finished') return t('secret.publish.voiceStateFinished', params)
  if (state === 'remaining') return t('secret.publish.voiceStateRemaining', params)
  if (state === 'processing') return t('secret.publish.voiceStateProcessing')
  if (state === 'preview') return t('secret.publish.voiceStatePreview')
  return t('secret.publish.voiceStateIdle')
}

export function createSecretSwitchCopy(t) {
  return {
    text: {
      prefix: t('secret.publish.switchToTextPrefix'),
      action: t('secret.publish.switchToTextAction'),
      suffix: t('secret.publish.switchToTextSuffix')
    },
    voice: {
      prefix: t('secret.publish.switchToVoicePrefix'),
      action: t('secret.publish.switchToVoiceAction'),
      suffix: t('secret.publish.switchToVoiceSuffix')
    }
  }
}
