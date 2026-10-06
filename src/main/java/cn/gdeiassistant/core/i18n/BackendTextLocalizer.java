package cn.gdeiassistant.core.i18n;

import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BackendTextLocalizer {

    private static final Pattern COMMENT_WITH_DETAIL = Pattern.compile("^(.+?) 评论了你的.+?[：:](.+)$");
    private static final Pattern DELIVERY_ACCEPTED = Pattern.compile("^(.+?) 接取了你发布的 (.+) 订单$");
    private static final Pattern DELIVERY_FINISHED = Pattern.compile("^(.+?) 已确认你接取的 (.+) 订单完成$");
    private static final Pattern DATING_ACCEPTED = Pattern.compile("^(.+?) 通过了你的请求$");
    private static final Pattern DATING_REJECTED = Pattern.compile("^(.+?) 拒绝了你的请求$");
    private static final Pattern DATING_PICK_WITH_DETAIL = Pattern.compile("^(.+?) 向你发起了撩一下[：:](.+)$");
    private static final Pattern DATING_PICK = Pattern.compile("^(.+?) 向你发起了撩一下$");
    private static final Pattern GUESS_CORRECT = Pattern.compile("^(.+?) 猜中了你的表白对象$");
    private static final Pattern GUESS_JOINED = Pattern.compile("^(.+?) 参与了你的猜名字$");
    private static final Pattern SIMPLE_LIKE = Pattern.compile("^(.+?) 点赞了你的.+$");
    private static final Pattern INVALID_FIELD = Pattern.compile("^([A-Za-z][A-Za-z0-9]*) 无效$");

    private static final Map<String, String> ZH_MESSAGE_KEY_MAP = new HashMap<>();
    private static final Map<String, Map<String, String>> MESSAGE_TRANSLATIONS = new HashMap<>();

    static {
        registerMessage("{0} 无效", "request.invalidField",
                "Invalid {0}", "{0} 唔啱", "{0} 無效",
                "{0} が無効です", "{0} 값이 올바르지 않습니다");
        registerMessage("未检测到有效令牌", "token.missing",
                "No valid sign-in token was found", "搵唔到有效登入憑證", "找不到有效的登入憑證",
                "有効なログイントークンが見つかりません", "유효한 로그인 토큰을 찾을 수 없습니다");
        registerMessage("缺少 sn 或 code", "library.renew.missingIdentifier",
                "The book identifier or renewal code is missing", "欠缺圖書編號或者續借代碼", "缺少圖書編號或續借代碼",
                "図書番号または更新コードがありません", "도서 번호 또는 연장 코드가 없습니다");
        registerMessage("请提供图书馆密码", "library.password.required",
                "Please enter your library password", "請輸入圖書館密碼", "請輸入圖書館密碼",
                "図書館のパスワードを入力してください", "도서관 비밀번호를 입력해 주세요");
        registerMessage("准考证号不能为空", "cet.ticket.required",
                "Please enter your admission ticket number", "請輸入准考證號碼", "請輸入准考證號碼",
                "受験番号を入力してください", "수험 번호를 입력해 주세요");
        registerMessage("准考证号必须为15位", "cet.ticket.length",
                "The admission ticket number must be 15 characters", "准考證號碼要有15位", "准考證號碼必須為15位",
                "受験番号は15桁で入力してください", "수험 번호는 15자리여야 합니다");
        registerMessage("准考证号必须为15位数字", "cet.ticket.digits",
                "The admission ticket number must be 15 digits", "准考證號碼要係15位數字", "准考證號碼必須為15位數字",
                "受験番号は15桁の数字で入力してください", "수험 번호는 15자리 숫자여야 합니다");
        registerMessage("请求体不能为空", "request.body.required",
                "Please provide the required request data", "請提供所需資料", "請提供必要的資料",
                "必要なデータを送信してください", "필수 데이터를 입력해 주세요");
        registerMessage("fileName 不能为空", "upload.fileName.required",
                "Please provide a file name", "請提供檔案名稱", "請提供檔案名稱",
                "ファイル名を指定してください", "파일 이름을 입력해 주세요");
        registerMessage("contentType 不合法", "upload.contentType.invalid",
                "The file content type is invalid", "檔案內容類型唔啱", "檔案內容類型無效",
                "ファイルのコンテンツタイプが無効です", "파일 콘텐츠 유형이 올바르지 않습니다");
        registerMessage("不支持的文件类型: ", "upload.type.unsupported",
                "Unsupported file type: ", "唔支援呢種檔案類型：", "不支援此檔案類型：",
                "サポートされていないファイル形式: ", "지원하지 않는 파일 유형: ");
        registerMessage("文件扩展名与 contentType 不匹配", "upload.extension.mismatch",
                "The file extension does not match its content type", "副檔名同檔案內容類型唔一致", "副檔名與檔案內容類型不一致",
                "拡張子がファイルのコンテンツタイプと一致しません", "파일 확장자가 콘텐츠 유형과 일치하지 않습니다");
        registerMessage("清理教务缓存时遇到网络延迟，设置已保存，可能需要稍后生效。", "privacy.saved.cacheDelayed",
                "Settings were saved. Clearing the academic cache was delayed, so changes may take effect later.",
                "設定已儲存，清理教務快取遇到網絡延遲，可能要等陣先生效。",
                "設定已儲存。清除教務快取時發生網路延遲，可能稍後才會生效。",
                "設定を保存しました。教務キャッシュの削除が遅れているため、反映まで時間がかかる場合があります。",
                "설정이 저장되었습니다. 학사 캐시 삭제가 지연되어 변경 사항이 나중에 적용될 수 있습니다.");
        registerMessage("请求参数不合法", "request.invalid",
                "Invalid request parameters",
                "提交嘅資料唔啱",
                "請求參數不合法",
                "リクエストパラメータが無効です",
                "요청 파라미터가 올바르지 않습니다");
        registerMessage("请求方法不支持", "request.methodUnsupported",
                "Unsupported request method",
                "唔支援呢種請求方式",
                "不支援的請求方法",
                "サポートされていないリクエストメソッドです",
                "지원하지 않는 요청 방식입니다");
        registerMessage("网络连接超时，请重试", "request.networkTimeoutRetry",
                "Network timeout, please try again",
                "網絡連線逾時，請再試",
                "網路連線逾時，請重試",
                "ネットワークがタイムアウトしました。もう一度お試しください",
                "네트워크 연결 시간이 초과되었습니다. 다시 시도해 주세요");
        registerMessage("查询条件不合法，请重新填写", "query.invalidConditionFilled",
                "Invalid query conditions, please check and try again",
                "查詢條件唔啱，請重新填寫",
                "查詢條件不合法，請重新填寫",
                "検索条件が無効です。入力内容をご確認ください",
                "조회 조건이 올바르지 않습니다. 다시 확인해 주세요");
        registerMessage("时间戳校验失败，请尝试重新登录", "request.timestampInvalid",
                "Timestamp validation failed, please sign in again",
                "時間戳驗證失敗，請重新登入",
                "時間戳校驗失敗，請嘗試重新登入",
                "タイムスタンプの検証に失敗しました。再度ログインしてください",
                "타임스탬프 검증에 실패했습니다. 다시 로그인해 주세요");
        registerMessage("用户账号密码错误，请检查重试或重新登录", "auth.passwordIncorrect",
                "Incorrect username or password, please check and try again",
                "帳號或者密碼唔啱，請檢查後再試",
                "使用者帳號或密碼錯誤，請檢查後重試",
                "ユーザー名またはパスワードが正しくありません。確認してもう一度お試しください",
                "아이디 또는 비밀번호가 올바르지 않습니다. 다시 확인해 주세요");
        registerMessage("账号或密码错误", "auth.passwordIncorrect.simple",
                "Incorrect username or password",
                "帳號或者密碼唔啱",
                "帳號或密碼錯誤",
                "アカウントまたはパスワードが正しくありません",
                "아이디 또는 비밀번호가 올바르지 않습니다");
        registerMessage("当前用户不存在，请尝试重新登录", "auth.userMissing",
                "Current user does not exist, please sign in again",
                "搵唔到呢個用戶，請重新登入",
                "目前使用者不存在，請嘗試重新登入",
                "現在のユーザーが存在しません。再度ログインしてください",
                "현재 사용자가 존재하지 않습니다. 다시 로그인해 주세요");
        registerMessage("该功能未启用", "feature.disabled",
                "This feature is not enabled",
                "呢個功能未開放",
                "該功能未啟用",
                "この機能は有効になっていません",
                "이 기능은 아직 활성화되지 않았습니다");
        registerMessage("系统繁忙，请稍后再试", "system.busy",
                "System is busy, please try again later",
                "系統忙緊，請等陣再試",
                "系統忙碌中，請稍後再試",
                "システムが混み合っています。しばらくしてから再度お試しください",
                "시스템이 바쁩니다. 잠시 후 다시 시도해 주세요");
        registerMessage("系统繁忙，登录失败，请稍后重试", "system.busy.loginFailed",
                "Sign-in failed because the system is busy. Please try again later",
                "系統忙緊，登入唔到，請等陣再試",
                "系統忙碌中，登入失敗，請稍後重試",
                "システムが混み合っているためログインできませんでした。しばらくしてからもう一度お試しください",
                "시스템이 바빠 로그인하지 못했습니다. 잠시 후 다시 시도해 주세요");
        registerMessage("当前学年暂不可查询", "grade.currentYearUnavailable",
                "Grades are not available for the current academic year",
                "暫時查唔到今個學年嘅成績",
                "當前學年暫時無法查詢",
                "現在の学年の成績はまだ照会できません",
                "현재 학년의 성적은 아직 조회할 수 없습니다");
        registerMessage("成绩数据更新成功", "grade.cache.updated",
                "Grade data refreshed successfully",
                "成績資料已更新",
                "成績資料更新成功",
                "成績データを更新しました",
                "성적 데이터를 성공적으로 새로고쳤습니다");
        registerMessage("当前学年成绩缓存数据不完整，请重新查询", "grade.cacheIncomplete",
                "Cached grades for the current academic year are incomplete, please refresh and try again",
                "今個學年嘅成績快取唔完整，請重新查詢",
                "當前學年成績快取資料不完整，請重新查詢",
                "現在の学年の成績キャッシュが不完全です。再度照会してください",
                "현재 학년의 성적 캐시 데이터가 완전하지 않습니다. 다시 조회해 주세요");
        registerMessage("教务系统异常", "grade.eduSystemError",
                "The academic system is temporarily unavailable",
                "教務系統暫時有問題",
                "教務系統異常",
                "教務システムで一時的なエラーが発生しました",
                "학사 시스템에 일시적인 오류가 발생했습니다");
        registerMessage("网络连接超时", "grade.networkTimeout",
                "Network timeout",
                "網絡連線逾時",
                "網路連線逾時",
                "ネットワークがタイムアウトしました",
                "네트워크 연결 시간이 초과되었습니다");
        registerMessage("用户密码错误", "grade.userPasswordIncorrect",
                "Incorrect password",
                "密碼唔啱",
                "使用者密碼錯誤",
                "パスワードが正しくありません",
                "비밀번호가 올바르지 않습니다");
        registerMessage("查询条件不可用", "query.conditionUnavailable",
                "Query conditions are not available",
                "呢個查詢條件用唔到",
                "查詢條件不可用",
                "検索条件を利用できません",
                "조회 조건을 사용할 수 없습니다");
        registerMessage("登录凭证已过期，请重新登录", "token.expired",
                "Your session has expired, please sign in again",
                "登入憑證已過期，請重新登入",
                "登入憑證已過期，請重新登入",
                "ログインセッションの有効期限が切れました。再度ログインしてください",
                "로그인 세션이 만료되었습니다. 다시 로그인해 주세요");
        registerMessage("可疑的登录请求，请退出账号并重新登录进行身份验证", "token.suspicious",
                "Suspicious sign-in request detected, please sign in again to verify your identity",
                "偵測到可疑登入，請重新登入驗證身份",
                "偵測到可疑登入請求，請重新登入以完成身分驗證",
                "不審なログインリクエストが検出されました。本人確認のため再度ログインしてください",
                "의심스러운 로그인 요청이 감지되었습니다. 본인 확인을 위해 다시 로그인해 주세요");
        registerMessage("没有对应的登录凭证记录，请尝试重新登录", "token.notFound",
                "No matching sign-in record was found, please sign in again",
                "搵唔到登入憑證，請重新登入",
                "沒有對應的登入憑證記錄，請嘗試重新登入",
                "一致するログイン記録が見つかりません。再度ログインしてください",
                "일치하는 로그인 기록이 없습니다. 다시 로그인해 주세요");
        registerMessage("登录凭证校验服务异常，请联系管理员", "token.serviceError",
                "The session validation service is unavailable, please contact the administrator",
                "登入驗證服務有問題，請聯絡管理員",
                "登入憑證校驗服務異常，請聯繫管理員",
                "ログイン検証サービスでエラーが発生しました。管理者にお問い合わせください",
                "로그인 검증 서비스에 오류가 발생했습니다. 관리자에게 문의해 주세요");
        registerMessage("上传的图片文件不合法", "profile.avatar.invalidFile",
                "The uploaded image file is invalid",
                "上載嘅圖片檔案唔啱",
                "上傳的圖片檔案不合法",
                "アップロードした画像ファイルが無効です",
                "업로드한 이미지 파일이 올바르지 않습니다");
        registerMessage("头像功能未启用", "profile.avatar.disabled",
                "Avatar upload is not enabled",
                "頭像功能未開放",
                "頭像功能未啟用",
                "アバター機能は有効になっていません",
                "아바타 기능이 활성화되어 있지 않습니다");
        registerMessage("用户不存在或未登录", "profile.user.notFoundOrLoggedOut",
                "The user was not found or is not signed in",
                "搵唔到呢個用戶，或者未登入",
                "使用者不存在或未登入",
                "ユーザーが見つからないか、ログインしていません",
                "사용자를 찾을 수 없거나 로그인되어 있지 않습니다");
        registerMessage("个人简介长度不合法", "profile.introduction.invalidLength",
                "The bio length is invalid",
                "個人簡介長度唔啱",
                "個人簡介長度不合法",
                "自己紹介の長さが無効です",
                "자기소개 길이가 올바르지 않습니다");
        registerMessage("请求参数异常", "request.parameterInvalid",
                "Request parameters are invalid",
                "提交嘅資料有問題",
                "請求參數異常",
                "リクエストパラメータが不正です",
                "요청 파라미터가 올바르지 않습니다");
        registerMessage("不合法的国家/地区代码", "profile.location.invalidRegion",
                "Invalid country or region code",
                "國家或者地區代碼唔啱",
                "不合法的國家或地區代碼",
                "国または地域コードが無効です",
                "국가 또는 지역 코드가 올바르지 않습니다");
        registerMessage("不合法的省/州代码", "profile.location.invalidState",
                "Invalid state or province code",
                "省或者州代碼唔啱",
                "不合法的省或州代碼",
                "州または省コードが無効です",
                "주 또는 성 코드가 올바르지 않습니다");
        registerMessage("不合法的市/直辖市代码", "profile.location.invalidCity",
                "Invalid city code",
                "城市或者直轄市代碼唔啱",
                "不合法的市或直轄市代碼",
                "市コードが無効です",
                "시 코드가 올바르지 않습니다");
        registerMessage("昵称长度不合法", "profile.nickname.invalidLength",
                "The nickname length is invalid",
                "暱稱長度唔啱",
                "暱稱長度不合法",
                "ニックネームの長さが無効です",
                "닉네임 길이가 올바르지 않습니다");
        registerMessage("没有空闲的课室", "spare.empty",
                "No spare classrooms are available",
                "冇空閒課室",
                "沒有空閒的課室",
                "空き教室はありません",
                "사용 가능한 빈 강의실이 없습니다");
        registerMessage("课表数据更新成功", "schedule.cache.updated",
                "Schedule data refreshed successfully",
                "課表資料已更新",
                "課表資料更新成功",
                "時間割データを更新しました",
                "시간표 데이터를 성공적으로 새로고쳤습니다");
        registerMessage("添加成功", "common.addSuccess",
                "Added successfully",
                "已新增",
                "新增成功",
                "追加しました",
                "추가했습니다");
        registerMessage("请求过于频繁，请稍后再试", "request.tooFrequent",
                "Too many requests, please try again later",
                "操作太頻密，請等陣再試",
                "請求過於頻繁，請稍後再試",
                "リクエストが多すぎます。しばらくしてから再度お試しください",
                "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요");
        registerMessage("不受支持的国际手机区号", "phone.areaCode.unsupported",
                "This international phone area code is not supported",
                "唔支援呢個國際電話區號",
                "不受支援的國際手機區號",
                "この国際電話番号の国番号には対応していません",
                "지원하지 않는 국제 전화 국가번호입니다");
        registerMessage("当前用户未绑定手机号", "phone.notBound",
                "The current user has not linked a phone number",
                "你仲未綁定手機號碼",
                "目前使用者尚未綁定手機號",
                "現在のユーザーは電話番号を連携していません",
                "현재 사용자는 전화번호를 연결하지 않았습니다");
        registerMessage("当前用户未绑定电子邮件", "email.notBound",
                "The current user has not linked an email address",
                "你仲未綁定電郵",
                "目前使用者尚未綁定電子郵件",
                "現在のユーザーはメールアドレスを連携していません",
                "현재 사용자는 이메일을 연결하지 않았습니다");
        registerMessage("24小时内已导出过用户数据，请勿重复提交请求", "userData.export.alreadySubmitted",
                "Your user data was already exported within the last 24 hours, please do not submit again",
                "24小時內已匯出過資料，請唔好重複提交",
                "24小時內已匯出過使用者資料，請勿重複提交請求",
                "過去24時間以内にユーザーデータをエクスポート済みです。再度送信しないでください",
                "최근 24시간 이내에 사용자 데이터를 이미 내보냈습니다. 다시 요청하지 마세요");
        registerMessage("系统正在导出用户数据，请稍候再返回下载", "userData.export.inProgress",
                "Your user data is being prepared, please come back later to download it",
                "系統正匯出緊資料，請等陣再返嚟下載",
                "系統正在匯出使用者資料，請稍後再回來下載",
                "ユーザーデータを準備中です。しばらくしてからダウンロードしてください",
                "사용자 데이터를 준비 중입니다. 잠시 후 다시 내려받아 주세요");
        registerMessage("请先提交用户数据导出请求", "userData.export.required",
                "Please submit a user data export request first",
                "請先提交資料匯出申請",
                "請先提交使用者資料匯出請求",
                "先にユーザーデータのエクスポート申請を送信してください",
                "먼저 사용자 데이터 내보내기 요청을 제출해 주세요");
        registerMessage("感谢您的反馈", "feedback.received",
                "Thanks for your feedback",
                "多謝你嘅意見",
                "感謝您的回饋",
                "フィードバックありがとうございます",
                "피드백 감사합니다");
        registerMessage("不合法的Cron请求", "cron.invalidRequest",
                "Invalid cron request",
                "Cron 請求唔啱",
                "不合法的 Cron 請求",
                "無効な Cron リクエストです",
                "잘못된 Cron 요청입니다");
        registerMessage("不合法的图片文件", "upload.invalidImage",
                "Invalid image file",
                "圖片檔案唔啱",
                "不合法的圖片檔案",
                "無効な画像ファイルです",
                "잘못된 이미지 파일입니다");
        registerMessage("不支持混合上传图片参数", "upload.mixedImageParamsUnsupported",
                "Mixed image upload parameters are not supported",
                "唔支援混合上載圖片參數",
                "不支援混合上傳圖片參數",
                "画像アップロードの混在パラメータには対応していません",
                "혼합된 이미지 업로드 파라미터는 지원하지 않습니다");
        registerMessage("上传失败", "upload.failed",
                "Upload failed",
                "上載失敗",
                "上傳失敗",
                "アップロードに失敗しました",
                "업로드에 실패했습니다");
        registerMessage("获取二手交易商品预览图失败", "marketplace.preview.failed",
                "Failed to load the marketplace item preview image",
                "攞唔到二手商品預覽圖",
                "取得二手交易商品預覽圖失敗",
                "中古商品プレビュー画像の取得に失敗しました",
                "중고거래 상품 미리보기 이미지를 불러오지 못했습니다");
        registerMessage("已下架的二手交易信息不能查看", "marketplace.offUnavailable",
                "Items that have been taken down cannot be viewed",
                "呢件二手商品已下架，睇唔到詳情",
                "已下架的二手交易資訊不能查看",
                "掲載終了した中古商品情報は閲覧できません",
                "내려간 중고거래 정보는 볼 수 없습니다");
        registerMessage("已出售的二手交易信息不能查看", "marketplace.soldUnavailable",
                "Sold marketplace items cannot be viewed",
                "呢件二手商品已賣出，睇唔到詳情",
                "已出售的二手交易資訊不能查看",
                "販売済みの中古商品情報は閲覧できません",
                "판매 완료된 중고거래 정보는 볼 수 없습니다");
        registerMessage("商品价格不合法", "marketplace.invalidPrice",
                "Invalid item price",
                "商品價格唔啱",
                "商品價格不合法",
                "商品価格が無効です",
                "상품 가격이 올바르지 않습니다");
        registerMessage("话题图片上传失败", "topic.imageUpload.failed",
                "Failed to upload topic images",
                "話題圖片上載失敗",
                "話題圖片上傳失敗",
                "トピック画像のアップロードに失敗しました",
                "토픽 이미지 업로드에 실패했습니다");
        registerMessage("树洞信息不能为空", "secret.content.required",
                "Secret content cannot be empty",
                "樹洞內容唔可以留空",
                "樹洞資訊不能為空",
                "シークレット投稿の内容は空にできません",
                "비밀글 내용은 비워 둘 수 없습니다");
        registerMessage("语音内容不能为空", "secret.voice.required",
                "Voice content cannot be empty",
                "語音內容唔可以留空",
                "語音內容不能為空",
                "音声内容は空にできません",
                "음성 내용은 비워 둘 수 없습니다");
        registerMessage("语音文件大小过大", "secret.voice.tooLarge",
                "The voice file is too large",
                "語音檔案太大",
                "語音檔案過大",
                "音声ファイルが大きすぎます",
                "음성 파일이 너무 큽니다");
        registerMessage("语音上传失败", "secret.voice.uploadFailed",
                "Failed to upload the voice message",
                "語音上載失敗",
                "語音上傳失敗",
                "音声アップロードに失敗しました",
                "음성 업로드에 실패했습니다");
        registerMessage("树洞信息类型不合法", "secret.type.invalid",
                "Invalid secret post type",
                "樹洞內容類型唔啱",
                "樹洞資訊類型不合法",
                "シークレット投稿の種類が無効です",
                "비밀글 유형이 올바르지 않습니다");
        registerMessage("查询的校园树洞信息不存在", "secret.notFound",
                "The requested secret post does not exist",
                "搵唔到呢條樹洞內容",
                "查詢的校園樹洞資訊不存在",
                "指定したシークレット投稿は存在しません",
                "조회한 비밀글이 존재하지 않습니다");
        registerMessage("文本内容超过限制", "content.textTooLong",
                "Text content exceeds the limit",
                "文字內容超出長度限制",
                "文本內容超過限制",
                "テキスト内容が上限を超えています",
                "텍스트 내용이 제한을 초과했습니다");
        registerMessage("拍好校园图片上传失败", "photograph.uploadFailed",
                "Failed to upload campus photos",
                "拍好校園圖片上載失敗",
                "拍好校園圖片上傳失敗",
                "キャンパス写真のアップロードに失敗しました",
                "캠퍼스 사진 업로드에 실패했습니다");
        registerMessage("校园新闻站点访问失败", "news.siteAccess.failed",
                "The campus news site is temporarily unavailable",
                "暫時連唔到校園新聞網站",
                "校園新聞站點訪問失敗",
                "キャンパスニュースサイトに一時的にアクセスできません",
                "캠퍼스 뉴스 사이트에 일시적으로 접근할 수 없습니다");
        registerMessage("教务系统页面结构异常，未找到学期下拉框", "edu.pageStructure.termSelectMissing",
                "The academic system page structure has changed and the term selector could not be found",
                "教務系統頁面有變，搵唔到學期選單",
                "教務系統頁面結構異常，未找到學期下拉選單",
                "教務システムのページ構成が変更され、学期セレクターが見つかりませんでした",
                "학사 시스템 페이지 구조가 바뀌어 학기 선택 항목을 찾지 못했습니다");
    }


    static {
        registerMessage("未登录或会话已失效", "social.authRequired",
                "Please sign in again", "你仲未登入，或者登入已失效", "尚未登入或登入已失效", "再度ログインしてください", "다시 로그인해 주세요");
        registerMessage("用户不存在", "social.userNotFound",
                "User not found", "搵唔到呢個用戶", "找不到使用者", "ユーザーが見つかりません", "사용자를 찾을 수 없습니다");
        registerMessage("会话不存在", "social.conversationNotFound",
                "Conversation not found", "搵唔到呢段對話", "找不到對話", "会話が見つかりません", "대화를 찾을 수 없습니다");
        registerMessage("无法联系该用户", "social.contactUnavailable",
                "This user cannot be contacted", "聯絡唔到呢個用戶", "無法聯絡此使用者", "このユーザーには連絡できません", "이 사용자에게 연락할 수 없습니다");
        registerMessage("对方隐私设置不允许私信", "social.privacyRestricted",
                "Their privacy settings do not allow direct messages", "對方嘅私隱設定唔允許私訊", "對方的隱私設定不允許私訊", "相手のプライバシー設定によりメッセージを送信できません", "상대방의 개인정보 설정으로 메시지를 보낼 수 없습니다");
        registerMessage("客户端消息冲突", "social.clientMessageConflict",
                "This message conflicts with an earlier submission", "呢則訊息同之前提交嘅內容有衝突", "此訊息與先前提交的內容衝突", "以前に送信したメッセージと内容が競合しています", "이전에 전송한 메시지와 내용이 충돌합니다");
        registerMessage("请求过于频繁", "social.rateLimited",
                "Too many requests. Please try again later", "操作太頻密，請等陣再試", "操作太頻繁，請稍後再試", "操作が多すぎます。しばらくしてから再度お試しください", "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요");
        registerMessage("图片私信暂不可用", "social.imageUnavailable",
                "Image messaging is temporarily unavailable", "暫時用唔到圖片私訊", "圖片私訊暫時無法使用", "画像メッセージは一時的に利用できません", "이미지 메시지를 일시적으로 사용할 수 없습니다");
        registerMessage("不能与自己创建会话", "social.cannotChatWithSelf",
                "You cannot start a conversation with yourself", "唔可以同自己開對話", "無法與自己建立對話", "自分との会話は作成できません", "자신과 대화를 시작할 수 없습니다");
        registerMessage("不能关注自己", "social.cannotFollowSelf",
                "You cannot follow yourself", "唔可以關注自己", "無法追蹤自己", "自分をフォローすることはできません", "자신을 팔로우할 수 없습니다");
        registerMessage("不能拉黑自己", "social.cannotBlockSelf",
                "You cannot block yourself", "唔可以封鎖自己", "無法封鎖自己", "自分をブロックすることはできません", "자신을 차단할 수 없습니다");
        registerMessage("消息发送冲突，请重试", "social.sendConflict",
                "The message could not be committed. Please retry", "訊息提交有衝突，請再試", "訊息提交衝突，請重試", "メッセージの送信が競合しました。再度お試しください", "메시지 전송이 충돌했습니다. 다시 시도해 주세요");
        registerMessage("消息长度须为 1–1000 个 Unicode 字符", "social.messageLength",
                "Messages must contain 1–1000 Unicode characters", "訊息要有1–1000個 Unicode 字元", "訊息必須包含1–1000個 Unicode 字元", "メッセージは1〜1000文字のUnicode文字で入力してください", "메시지는 유니코드 문자 1–1000자로 입력해 주세요");
        registerMessage("图片不能为空", "social.imageRequired",
                "Please choose an image", "請揀一張圖片", "請選擇圖片", "画像を選択してください", "이미지를 선택해 주세요");
        registerMessage("image 必填", "social.imageFieldRequired",
                "Please choose an image", "請揀一張圖片", "請選擇圖片", "画像を選択してください", "이미지를 선택해 주세요");
        registerMessage("图片不能超过 5 MiB", "social.imageTooLarge",
                "Images must not exceed 5 MiB", "圖片唔可以超過5 MiB", "圖片不得超過5 MiB", "画像は5 MiB以下にしてください", "이미지는 5 MiB 이하여야 합니다");
        registerMessage("规范化后图片不能超过 5 MiB", "social.processedImageTooLarge",
                "The processed image must not exceed 5 MiB", "處理後嘅圖片唔可以超過5 MiB", "處理後的圖片不得超過5 MiB", "処理後の画像は5 MiB以下にしてください", "처리된 이미지는 5 MiB 이하여야 합니다");
        registerMessage("仅支持 JPEG/PNG 图片", "social.imageFormat",
                "Only JPEG and PNG images are supported", "只支援 JPEG 同 PNG 圖片", "僅支援 JPEG 與 PNG 圖片", "JPEGとPNG画像のみ対応しています", "JPEG 및 PNG 이미지만 지원합니다");
        registerMessage("无法读取图片", "social.imageUnreadable",
                "The image could not be read", "讀唔到呢張圖片", "無法讀取圖片", "画像を読み込めません", "이미지를 읽을 수 없습니다");
        registerMessage("无法解码图片", "social.imageUndecodable",
                "The image could not be decoded", "解碼唔到呢張圖片", "無法解碼圖片", "画像をデコードできません", "이미지를 디코딩할 수 없습니다");
        registerMessage("图片解码失败", "social.imageDecodeFailed",
                "The image could not be decoded", "圖片解碼失敗", "圖片解碼失敗", "画像のデコードに失敗しました", "이미지 디코딩에 실패했습니다");
        registerMessage("图片尺寸无效", "social.imageDimensions",
                "The image dimensions are invalid", "圖片尺寸唔啱", "圖片尺寸無效", "画像のサイズが無効です", "이미지 크기가 올바르지 않습니다");
        registerMessage("图片边长不能超过 4096", "social.imageEdgeLimit",
                "Neither image dimension may exceed 4096 pixels", "圖片邊長唔可以超過4096像素", "圖片邊長不得超過4096像素", "画像の各辺は4096ピクセル以下にしてください", "이미지의 각 변은 4096픽셀 이하여야 합니다");
        registerMessage("图片像素不能超过 1600 万", "social.imagePixelLimit",
                "Images must not exceed 16 million pixels", "圖片唔可以超過1600萬像素", "圖片不得超過1600萬像素", "画像は1600万画素以下にしてください", "이미지는 1600만 픽셀 이하여야 합니다");
        registerMessage("图片 MIME 与实际格式不一致", "social.imageMimeMismatch",
                "The image content type does not match its actual format", "圖片內容類型同實際格式唔一致", "圖片內容類型與實際格式不一致", "画像のコンテンツタイプと実際の形式が一致しません", "이미지 콘텐츠 유형이 실제 형식과 일치하지 않습니다");
        registerMessage("不支持 SVG 图片", "social.imageSvgUnsupported",
                "SVG images are not supported", "唔支援 SVG 圖片", "不支援 SVG 圖片", "SVG画像には対応していません", "SVG 이미지는 지원하지 않습니다");
        registerMessage("不支持 GIF 图片", "social.imageGifUnsupported",
                "GIF images are not supported", "唔支援 GIF 圖片", "不支援 GIF 圖片", "GIF画像には対応していません", "GIF 이미지는 지원하지 않습니다");
        registerMessage("不支持 WebP 图片", "social.imageWebpUnsupported",
                "WebP images are not supported", "唔支援 WebP 圖片", "不支援 WebP 圖片", "WebP画像には対応していません", "WebP 이미지는 지원하지 않습니다");
        registerMessage("当前环境不支持该图片格式", "social.imageFormatUnavailable",
                "This environment does not support this image format", "而家嘅環境唔支援呢種圖片格式", "目前環境不支援此圖片格式", "現在の環境ではこの画像形式に対応していません", "현재 환경은 이 이미지 형식을 지원하지 않습니다");
        registerMessage("当前环境无法重编码图片", "social.imageEncodingUnavailable",
                "This environment cannot process the image", "而家嘅環境處理唔到呢張圖片", "目前環境無法處理此圖片", "現在の環境では画像を処理できません", "현재 환경은 이미지를 처리할 수 없습니다");
        registerMessage("cursor 无效", "social.cursorInvalid",
                "The pagination cursor is invalid", "分頁位置唔啱", "分頁位置無效", "ページの位置が無効です", "페이지 위치가 올바르지 않습니다");
        registerMessage("dmPolicy 无效", "social.dmPolicyInvalid",
                "The direct message privacy option is invalid", "私訊私隱選項唔啱", "私訊隱私選項無效", "メッセージのプライバシー設定が無効です", "메시지 개인정보 설정이 올바르지 않습니다");
        registerMessage("kind 必须为 following|followers|friends", "social.relationshipKindInvalid",
                "Please select following, followers, or friends", "請揀關注、粉絲或者好友", "請選擇追蹤、粉絲或好友", "フォロー中、フォロワー、友だちから選択してください", "팔로잉, 팔로워 또는 친구를 선택해 주세요");
        registerMessage("clientMessageId 必填", "social.messageIdRequired",
                "A message identifier is required", "請提供訊息識別碼", "請提供訊息識別碼", "メッセージIDが必要です", "메시지 식별자가 필요합니다");
        registerMessage("clientMessageId 必须为 UUID", "social.messageIdInvalid",
                "The message identifier must be a valid UUID", "訊息識別碼要係有效 UUID", "訊息識別碼必須為有效的 UUID", "メッセージIDは有効なUUIDで指定してください", "메시지 식별자는 유효한 UUID여야 합니다");
        registerMessage("beforeSeq 与 afterSeq 互斥", "social.paginationDirectionConflict",
                "Please select only one pagination direction", "請只揀一個分頁方向", "請僅選擇一個分頁方向", "ページの移動方向は1つだけ指定してください", "페이지 이동 방향은 하나만 선택해 주세요");
        registerMessage("lastReadSeq 无效", "social.readPositionInvalid",
                "The read position is invalid", "已讀位置唔啱", "已讀位置無效", "既読位置が無効です", "읽음 위치가 올바르지 않습니다");
        registerMessage("lastReadSeq 不能超过已提交序号", "social.readPositionBeyondLatest",
                "The read position exceeds the latest message", "已讀位置唔可以超過最新訊息", "已讀位置不得超過最新訊息", "既読位置が最新のメッセージを超えています", "읽음 위치가 최신 메시지를 초과합니다");
    }

    private BackendTextLocalizer() {
    }

    public static String localizeMessage(String message, String language) {
        if (message == null || message.isBlank()) {
            return message;
        }
        String normalizedLanguage = ApiLanguageResolver.normalizeLanguage(language);
        if ("zh-CN".equals(normalizedLanguage)) {
            return message;
        }
        String key = ZH_MESSAGE_KEY_MAP.get(message);
        if (key != null) {
            return translate(key, normalizedLanguage, message);
        }
        Matcher invalidField = INVALID_FIELD.matcher(message);
        if (invalidField.matches()) {
            return template("request.invalidField", normalizedLanguage, invalidField.group(1));
        }
        if ("zh-HK".equals(normalizedLanguage) || "zh-TW".equals(normalizedLanguage)) {
            return ZhConverterUtil.toTraditional(message);
        }
        return message;
    }

    public static InteractionMessageVO localizeInteractionMessage(InteractionMessageVO source, String language) {
        if (source == null) {
            return null;
        }
        String normalizedLanguage = ApiLanguageResolver.normalizeLanguage(language);
        if ("zh-CN".equals(normalizedLanguage)) {
            return source;
        }
        InteractionMessageVO localized = new InteractionMessageVO();
        localized.setId(source.getId());
        localized.setModule(source.getModule());
        localized.setType(source.getType());
        localized.setCreatedAt(source.getCreatedAt());
        localized.setIsRead(source.getIsRead());
        localized.setTargetType(source.getTargetType());
        localized.setTargetId(source.getTargetId());
        localized.setTargetSubId(source.getTargetSubId());
        localized.setTitle(localizeInteractionTitle(source.getModule(), source.getType(), source.getTitle(), normalizedLanguage));
        localized.setContent(localizeInteractionContent(source.getModule(), source.getType(), source.getContent(), normalizedLanguage));
        return localized;
    }

    private static String localizeInteractionTitle(String module, String type, String fallbackTitle, String language) {
        String key = switch ((safe(module) + ":" + safe(type))) {
            case "secret:comment" -> "interaction.secret.comment.title";
            case "secret:like" -> "interaction.secret.like.title";
            case "express:comment" -> "interaction.express.comment.title";
            case "express:like" -> "interaction.express.like.title";
            case "express:guess" -> "interaction.express.guess.title";
            case "topic:like" -> "interaction.topic.like.title";
            case "photograph:comment" -> "interaction.photograph.comment.title";
            case "photograph:like" -> "interaction.photograph.like.title";
            case "dating:pick_received" -> "interaction.dating.received.title";
            case "dating:pick_accepted" -> "interaction.dating.accepted.title";
            case "dating:pick_rejected" -> "interaction.dating.rejected.title";
            case "delivery:order_accepted" -> "interaction.delivery.accepted.title";
            case "delivery:order_finished" -> "interaction.delivery.finished.title";
            default -> null;
        };
        if (key == null) {
            return localizeMessage(fallbackTitle, language);
        }
        return translate(key, language, localizeMessage(fallbackTitle, language));
    }

    private static String localizeInteractionContent(String module, String type, String content, String language) {
        if (content == null || content.isBlank()) {
            return content;
        }
        String key = safe(module) + ":" + safe(type);
        return switch (key) {
            case "secret:comment" -> formatComment(content, language, "interaction.secret.comment.content", "interaction.secret.comment.contentWithDetail");
            case "secret:like" -> formatActorOnly(content, SIMPLE_LIKE, language, "interaction.secret.like.content");
            case "express:comment" -> formatComment(content, language, "interaction.express.comment.content", "interaction.express.comment.contentWithDetail");
            case "express:like" -> formatActorOnly(content, SIMPLE_LIKE, language, "interaction.express.like.content");
            case "express:guess" -> formatGuess(content, language);
            case "topic:like" -> formatActorOnly(content, SIMPLE_LIKE, language, "interaction.topic.like.content");
            case "photograph:comment" -> formatComment(content, language, "interaction.photograph.comment.content", "interaction.photograph.comment.contentWithDetail");
            case "photograph:like" -> formatActorOnly(content, SIMPLE_LIKE, language, "interaction.photograph.like.content");
            case "dating:pick_received" -> formatDatingPick(content, language);
            case "dating:pick_accepted" -> formatNickname(content, DATING_ACCEPTED, language, "interaction.dating.accepted.content");
            case "dating:pick_rejected" -> formatNickname(content, DATING_REJECTED, language, "interaction.dating.rejected.content");
            case "delivery:order_accepted" -> formatDelivery(content, DELIVERY_ACCEPTED, language, "interaction.delivery.accepted.content", "interaction.delivery.accepted.contentWithCompany");
            case "delivery:order_finished" -> formatDelivery(content, DELIVERY_FINISHED, language, "interaction.delivery.finished.content", "interaction.delivery.finished.contentWithCompany");
            default -> localizeMessage(content, language);
        };
    }

    private static String formatComment(String content, String language, String baseKey, String detailKey) {
        Matcher matcher = COMMENT_WITH_DETAIL.matcher(content);
        if (matcher.matches()) {
            return template(detailKey, language, matcher.group(1), matcher.group(2).trim());
        }
        return localizeMessage(content, language);
    }

    private static String formatActorOnly(String content, Pattern pattern, String language, String key) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.matches()) {
            return template(key, language, matcher.group(1));
        }
        return localizeMessage(content, language);
    }

    private static String formatDatingPick(String content, String language) {
        Matcher detailMatcher = DATING_PICK_WITH_DETAIL.matcher(content);
        if (detailMatcher.matches()) {
            return template("interaction.dating.received.contentWithDetail", language, detailMatcher.group(1), detailMatcher.group(2).trim());
        }
        Matcher matcher = DATING_PICK.matcher(content);
        if (matcher.matches()) {
            return template("interaction.dating.received.content", language, matcher.group(1));
        }
        return localizeMessage(content, language);
    }

    private static String formatNickname(String content, Pattern pattern, String language, String key) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.matches()) {
            return template(key, language, matcher.group(1));
        }
        return localizeMessage(content, language);
    }

    private static String formatDelivery(String content, Pattern pattern, String language, String actorKey, String actorCompanyKey) {
        Matcher matcher = pattern.matcher(content);
        if (!matcher.matches()) {
            return localizeMessage(content, language);
        }
        String actor = matcher.group(1);
        String company = matcher.groupCount() >= 2 ? matcher.group(2) : null;
        if (company == null || company.isBlank()) {
            return template(actorKey, language, actor);
        }
        return template(actorCompanyKey, language, actor, company.trim());
    }

    private static String formatGuess(String content, String language) {
        Matcher correctMatcher = GUESS_CORRECT.matcher(content);
        if (correctMatcher.matches()) {
            return template("interaction.express.guess.correct", language, correctMatcher.group(1));
        }
        Matcher joinedMatcher = GUESS_JOINED.matcher(content);
        if (joinedMatcher.matches()) {
            return template("interaction.express.guess.joined", language, joinedMatcher.group(1));
        }
        return localizeMessage(content, language);
    }

    private static void registerMessage(String zhCn, String key, String en, String zhHk, String zhTw, String ja, String ko) {
        ZH_MESSAGE_KEY_MAP.put(zhCn, key);
        MESSAGE_TRANSLATIONS.put(key, Map.of(
                "zh-CN", zhCn,
                "zh-HK", zhHk,
                "zh-TW", zhTw,
                "en", en,
                "ja", ja,
                "ko", ko
        ));
    }

    private static String translate(String key, String language, String fallback) {
        Map<String, String> translations = MESSAGE_TRANSLATIONS.get(key);
        if (translations == null) {
            return fallback;
        }
        return translations.getOrDefault(language, translations.getOrDefault("zh-CN", fallback));
    }

    private static String template(String key, String language, Object... args) {
        String template = translate(key, language, key);
        if (args == null || args.length == 0) {
            return template;
        }
        return java.text.MessageFormat.format(template, args);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    static {
        MESSAGE_TRANSLATIONS.put("interaction.secret.comment.title", Map.of(
                "zh-CN", "树洞收到新评论",
                "zh-HK", "樹洞收到新評論",
                "zh-TW", "樹洞收到新評論",
                "en", "Your confession received a new comment",
                "ja", "ツリーホールに新しいコメントがありました",
                "ko", "트리홀에 새 댓글이 달렸습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.secret.like.title", Map.of(
                "zh-CN", "树洞收到新点赞",
                "zh-HK", "樹洞收到新點讚",
                "zh-TW", "樹洞收到新按讚",
                "en", "Your confession received a new like",
                "ja", "ツリーホールに新しいいいねがありました",
                "ko", "트리홀에 새 좋아요가 생겼습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.secret.comment.contentWithDetail", Map.of(
                "zh-CN", "{0} 评论了你的树洞：{1}",
                "zh-HK", "{0} 留言回應咗你嘅樹洞：{1}",
                "zh-TW", "{0} 評論了你的樹洞：{1}",
                "en", "{0} commented on your confession: {1}",
                "ja", "{0} さんがあなたのツリーホールにコメントしました: {1}",
                "ko", "{0}님이 당신의 트리홀에 댓글을 남겼습니다: {1}"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.secret.like.content", Map.of(
                "zh-CN", "{0} 点赞了你的树洞",
                "zh-HK", "{0} 讚咗你嘅樹洞",
                "zh-TW", "{0} 按讚了你的樹洞",
                "en", "{0} liked your confession",
                "ja", "{0} さんがあなたのツリーホールにいいねしました",
                "ko", "{0}님이 당신의 트리홀을 좋아합니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.comment.title", Map.of(
                "zh-CN", "表白墙收到新评论",
                "zh-HK", "表白牆收到新評論",
                "zh-TW", "表白牆收到新評論",
                "en", "Your confession wall post received a new comment",
                "ja", "告白ウォールに新しいコメントがありました",
                "ko", "고백 게시판에 새 댓글이 달렸습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.like.title", Map.of(
                "zh-CN", "表白墙收到新点赞",
                "zh-HK", "表白牆收到新點讚",
                "zh-TW", "表白牆收到新按讚",
                "en", "Your confession wall post received a new like",
                "ja", "告白ウォールに新しいいいねがありました",
                "ko", "고백 게시판에 새 좋아요가 생겼습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.guess.title", Map.of(
                "zh-CN", "表白墙有人参与猜名字",
                "zh-HK", "有人參與咗你嘅表白牆估名活動",
                "zh-TW", "表白牆有人參與猜名字",
                "en", "Someone joined your guess-the-name interaction",
                "ja", "告白ウォールの名前当てに参加した人がいます",
                "ko", "누군가 이름 맞히기 활동에 참여했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.comment.contentWithDetail", Map.of(
                "zh-CN", "{0} 评论了你的表白：{1}",
                "zh-HK", "{0} 留言回應咗你嘅表白：{1}",
                "zh-TW", "{0} 評論了你的表白：{1}",
                "en", "{0} commented on your confession wall post: {1}",
                "ja", "{0} さんがあなたの告白にコメントしました: {1}",
                "ko", "{0}님이 당신의 고백 글에 댓글을 남겼습니다: {1}"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.like.content", Map.of(
                "zh-CN", "{0} 点赞了你的表白",
                "zh-HK", "{0} 讚咗你嘅表白",
                "zh-TW", "{0} 按讚了你的表白",
                "en", "{0} liked your confession wall post",
                "ja", "{0} さんがあなたの告白にいいねしました",
                "ko", "{0}님이 당신의 고백 글을 좋아합니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.guess.correct", Map.of(
                "zh-CN", "{0} 猜中了你的表白对象",
                "zh-HK", "{0} 估中咗你嘅表白對象",
                "zh-TW", "{0} 猜中了你的表白對象",
                "en", "{0} guessed your crush correctly",
                "ja", "{0} さんがあなたの想い人を正しく当てました",
                "ko", "{0}님이 당신의 짝사랑 상대를 맞혔습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.guess.joined", Map.of(
                "zh-CN", "{0} 参与了你的猜名字",
                "zh-HK", "{0} 參與咗你嘅估名活動",
                "zh-TW", "{0} 參與了你的猜名字",
                "en", "{0} joined your guess-the-name interaction",
                "ja", "{0} さんがあなたの名前当てに参加しました",
                "ko", "{0}님이 당신의 이름 맞히기 활동에 참여했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.topic.like.title", Map.of(
                "zh-CN", "话题收到新点赞",
                "zh-HK", "話題收到新點讚",
                "zh-TW", "話題收到新按讚",
                "en", "Your topic received a new like",
                "ja", "話題に新しいいいねがありました",
                "ko", "주제에 새 좋아요가 생겼습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.topic.like.content", Map.of(
                "zh-CN", "{0} 点赞了你的话题",
                "zh-HK", "{0} 讚咗你嘅話題",
                "zh-TW", "{0} 按讚了你的話題",
                "en", "{0} liked your topic",
                "ja", "{0} さんがあなたの話題にいいねしました",
                "ko", "{0}님이 당신의 주제를 좋아합니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.photograph.comment.title", Map.of(
                "zh-CN", "作品收到新评论",
                "zh-HK", "作品收到新評論",
                "zh-TW", "作品收到新評論",
                "en", "Your post received a new comment",
                "ja", "作品に新しいコメントがありました",
                "ko", "작품에 새 댓글이 달렸습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.photograph.like.title", Map.of(
                "zh-CN", "作品收到新点赞",
                "zh-HK", "作品收到新點讚",
                "zh-TW", "作品收到新按讚",
                "en", "Your post received a new like",
                "ja", "作品に新しいいいねがありました",
                "ko", "작품에 새 좋아요가 생겼습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.photograph.comment.contentWithDetail", Map.of(
                "zh-CN", "{0} 评论了你的作品：{1}",
                "zh-HK", "{0} 留言回應咗你嘅作品：{1}",
                "zh-TW", "{0} 評論了你的作品：{1}",
                "en", "{0} commented on your post: {1}",
                "ja", "{0} さんがあなたの作品にコメントしました: {1}",
                "ko", "{0}님이 당신의 작품에 댓글을 남겼습니다: {1}"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.photograph.like.content", Map.of(
                "zh-CN", "{0} 点赞了你的作品",
                "zh-HK", "{0} 讚咗你嘅作品",
                "zh-TW", "{0} 按讚了你的作品",
                "en", "{0} liked your post",
                "ja", "{0} さんがあなたの作品にいいねしました",
                "ko", "{0}님이 당신의 작품을 좋아합니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.received.title", Map.of(
                "zh-CN", "收到新的撩一下",
                "zh-HK", "收到新的撩一下",
                "zh-TW", "收到新的撩一下",
                "en", "You received a new poke",
                "ja", "新しいアプローチが届きました",
                "ko", "새로운 찔러보기가 도착했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.accepted.title", Map.of(
                "zh-CN", "撩一下已通过",
                "zh-HK", "撩一下請求已接受",
                "zh-TW", "撩一下已通過",
                "en", "Your poke was accepted",
                "ja", "アプローチが承認されました",
                "ko", "찔러보기가 수락되었습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.rejected.title", Map.of(
                "zh-CN", "撩一下未通过",
                "zh-HK", "撩一下請求未獲接受",
                "zh-TW", "撩一下未通過",
                "en", "Your poke was declined",
                "ja", "アプローチは承認されませんでした",
                "ko", "찔러보기가 거절되었습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.received.content", Map.of(
                "zh-CN", "{0} 向你发起了撩一下",
                "zh-HK", "{0} 想同你撩一下",
                "zh-TW", "{0} 向你發起了撩一下",
                "en", "{0} sent you a poke",
                "ja", "{0} さんがあなたにアプローチしました",
                "ko", "{0}님이 당신에게 찔러보기를 보냈습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.received.contentWithDetail", Map.of(
                "zh-CN", "{0} 向你发起了撩一下：{1}",
                "zh-HK", "{0} 想同你撩一下：{1}",
                "zh-TW", "{0} 向你發起了撩一下：{1}",
                "en", "{0} sent you a poke: {1}",
                "ja", "{0} さんがあなたにアプローチしました: {1}",
                "ko", "{0}님이 당신에게 찔러보기를 보냈습니다: {1}"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.accepted.content", Map.of(
                "zh-CN", "{0} 通过了你的请求",
                "zh-HK", "{0} 接受咗你嘅請求",
                "zh-TW", "{0} 通過了你的請求",
                "en", "{0} accepted your request",
                "ja", "{0} さんがあなたのリクエストを承認しました",
                "ko", "{0}님이 당신의 요청을 수락했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.rejected.content", Map.of(
                "zh-CN", "{0} 拒绝了你的请求",
                "zh-HK", "{0} 拒絕咗你嘅請求",
                "zh-TW", "{0} 拒絕了你的請求",
                "en", "{0} declined your request",
                "ja", "{0} さんがあなたのリクエストを辞退しました",
                "ko", "{0}님이 당신의 요청을 거절했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.accepted.title", Map.of(
                "zh-CN", "订单已被接单",
                "zh-HK", "訂單有人接咗",
                "zh-TW", "訂單已被接單",
                "en", "Your order was accepted",
                "ja", "注文が受け付けられました",
                "ko", "주문이 접수되었습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.finished.title", Map.of(
                "zh-CN", "订单已完成",
                "zh-HK", "訂單已完成",
                "zh-TW", "訂單已完成",
                "en", "Your order was completed",
                "ja", "注文が完了しました",
                "ko", "주문이 완료되었습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.accepted.content", Map.of(
                "zh-CN", "{0} 接取了你的订单",
                "zh-HK", "{0} 接咗你嘅訂單",
                "zh-TW", "{0} 接取了你的訂單",
                "en", "{0} accepted your order",
                "ja", "{0} さんがあなたの注文を受けました",
                "ko", "{0}님이 당신의 주문을 접수했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.accepted.contentWithCompany", Map.of(
                "zh-CN", "{0} 接取了你发布的 {1} 订单",
                "zh-HK", "{0} 接咗你發佈嘅 {1} 訂單",
                "zh-TW", "{0} 接取了你發布的 {1} 訂單",
                "en", "{0} accepted your {1} order",
                "ja", "{0} さんがあなたの {1} 注文を受けました",
                "ko", "{0}님이 당신의 {1} 주문을 접수했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.finished.content", Map.of(
                "zh-CN", "{0} 确认你的订单已完成",
                "zh-HK", "{0} 確認咗你嘅訂單已完成",
                "zh-TW", "{0} 確認你的訂單已完成",
                "en", "{0} confirmed your order was completed",
                "ja", "{0} さんがあなたの注文完了を確認しました",
                "ko", "{0}님이 당신의 주문 완료를 확인했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.finished.contentWithCompany", Map.of(
                "zh-CN", "{0} 已确认你接取的 {1} 订单完成",
                "zh-HK", "{0} 確認咗你接嘅 {1} 訂單已完成",
                "zh-TW", "{0} 已確認你接取的 {1} 訂單完成",
                "en", "{0} confirmed your {1} order was completed",
                "ja", "{0} さんがあなたの {1} 注文完了を確認しました",
                "ko", "{0}님이 당신의 {1} 주문 완료를 확인했습니다"
        ));
    }
}
