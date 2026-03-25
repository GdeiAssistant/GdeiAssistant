export function createCampusOptions(t) {
  return [
    { label: t('spare.option.unlimited'), value: 0 },
    { label: t('spare.campus.haizhu'), value: 1 },
    { label: t('spare.campus.huadu'), value: 2 },
    { label: t('spare.campus.nanhai'), value: 3 },
    { label: t('spare.campus.correspondence'), value: 4 }
  ]
}

export function createRoomTypeOptions(t) {
  const keys = [
    'unlimited', 'noClassroom', 'playground', 'largeMultimedia', 'computerLab', 'sculptureRoom',
    'multimediaRoom', 'translationRoom', 'fashionLab', 'pianoRoomA', 'pianoRoomB', 'publicComputerRoom',
    'traditionalPaintingRoom', 'artStudio', 'chemistryLab', 'computerRoom', 'teachingAidRoom', 'educationLab',
    'anatomyLab', 'financialMathLab', 'artClassroom', 'montessoriRoom', 'modelMakingLab', 'graphicDesignLab',
    'musicPracticeRoom', 'photographyLab', 'vocalRoom', 'biologyLab', 'trainingRoom', 'solfeggioRoom',
    'ceramicRoom', 'gymnasticsRoom', 'networkLab', 'microteachingRoom', 'noNeedClassroom', 'danceRoomA',
    'danceRoomB', 'physicsLab', 'smallMultimedia', 'smallMultimediaUnder70', 'smallClassroomUnder70',
    'groupClassroom', 'bodyTrainingRoom', 'musicRoom', 'musicMajorRoom', 'languageLab', 'smartRecordingRoom',
    'mediumMultimedia', 'majorClassroom', 'majorTheoryRoom', 'majorLab', 'consultingRoom', 'integratedPaintingLab'
  ]

  return keys.map((key, index) => ({
    label: t(`spare.roomType.${key}`),
    value: index
  }))
}

export function createWeekDayOptions(t) {
  return [
    { label: t('spare.option.unlimited'), value: -1 },
    { label: t('spare.weekday.monday'), value: 0 },
    { label: t('spare.weekday.tuesday'), value: 1 },
    { label: t('spare.weekday.wednesday'), value: 2 },
    { label: t('spare.weekday.thursday'), value: 3 },
    { label: t('spare.weekday.friday'), value: 4 },
    { label: t('spare.weekday.saturday'), value: 5 },
    { label: t('spare.weekday.sunday'), value: 6 }
  ]
}

export function createSingleDoubleOptions(t) {
  return [
    { label: t('spare.option.unlimited'), value: 0 },
    { label: t('spare.weekType.single'), value: 1 },
    { label: t('spare.weekType.double'), value: 2 }
  ]
}

export function createPeriodOptions(t) {
  return [
    { label: t('spare.period.oneTwo'), value: 0 },
    { label: t('spare.period.three'), value: 1 },
    { label: t('spare.period.fourFive'), value: 2 },
    { label: t('spare.period.sixSeven'), value: 3 },
    { label: t('spare.period.eightNine'), value: 4 },
    { label: t('spare.period.tenToTwelve'), value: 5 },
    { label: t('spare.period.morning'), value: 6 },
    { label: t('spare.period.afternoon'), value: 7 },
    { label: t('spare.period.evening'), value: 8 },
    { label: t('spare.period.daytime'), value: 9 },
    { label: t('spare.period.allDay'), value: 10 }
  ]
}
