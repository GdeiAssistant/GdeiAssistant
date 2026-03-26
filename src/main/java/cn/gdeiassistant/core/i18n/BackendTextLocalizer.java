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

    private static final Map<String, String> ZH_MESSAGE_KEY_MAP = new HashMap<>();
    private static final Map<String, Map<String, String>> MESSAGE_TRANSLATIONS = new HashMap<>();

    static {
        registerMessage("请求参数不合法", "request.invalid",
                "Invalid request parameters",
                "請求參數不合法",
                "請求參數不合法",
                "リクエストパラメータが無効です",
                "요청 파라미터가 올바르지 않습니다");
        registerMessage("请求方法不支持", "request.methodUnsupported",
                "Unsupported request method",
                "不支援的請求方法",
                "不支援的請求方法",
                "サポートされていないリクエストメソッドです",
                "지원하지 않는 요청 방식입니다");
        registerMessage("网络连接超时，请重试", "request.networkTimeoutRetry",
                "Network timeout, please try again",
                "網絡連接超時，請重試",
                "網路連線逾時，請重試",
                "ネットワークがタイムアウトしました。もう一度お試しください",
                "네트워크 연결 시간이 초과되었습니다. 다시 시도해 주세요");
        registerMessage("查询条件不合法，请重新填写", "query.invalidConditionFilled",
                "Invalid query conditions, please check and try again",
                "查詢條件不合法，請重新填寫",
                "查詢條件不合法，請重新填寫",
                "検索条件が無効です。入力内容をご確認ください",
                "조회 조건이 올바르지 않습니다. 다시 확인해 주세요");
        registerMessage("时间戳校验失败，请尝试重新登录", "request.timestampInvalid",
                "Timestamp validation failed, please sign in again",
                "時間戳校驗失敗，請嘗試重新登入",
                "時間戳校驗失敗，請嘗試重新登入",
                "タイムスタンプの検証に失敗しました。再度ログインしてください",
                "타임스탬프 검증에 실패했습니다. 다시 로그인해 주세요");
        registerMessage("用户账号密码错误，请检查重试或重新登录", "auth.passwordIncorrect",
                "Incorrect username or password, please check and try again",
                "用戶賬號或密碼錯誤，請檢查後重試",
                "使用者帳號或密碼錯誤，請檢查後重試",
                "ユーザー名またはパスワードが正しくありません。確認してもう一度お試しください",
                "아이디 또는 비밀번호가 올바르지 않습니다. 다시 확인해 주세요");
        registerMessage("账号或密码错误", "auth.passwordIncorrect.simple",
                "Incorrect username or password",
                "賬號或密碼錯誤",
                "帳號或密碼錯誤",
                "アカウントまたはパスワードが正しくありません",
                "아이디 또는 비밀번호가 올바르지 않습니다");
        registerMessage("当前用户不存在，请尝试重新登录", "auth.userMissing",
                "Current user does not exist, please sign in again",
                "當前用戶不存在，請嘗試重新登入",
                "目前使用者不存在，請嘗試重新登入",
                "現在のユーザーが存在しません。再度ログインしてください",
                "현재 사용자가 존재하지 않습니다. 다시 로그인해 주세요");
        registerMessage("该功能未启用", "feature.disabled",
                "This feature is not enabled",
                "該功能未啟用",
                "該功能未啟用",
                "この機能は有効になっていません",
                "이 기능은 아직 활성화되지 않았습니다");
        registerMessage("系统繁忙，请稍后再试", "system.busy",
                "System is busy, please try again later",
                "系統繁忙，請稍後再試",
                "系統忙碌中，請稍後再試",
                "システムが混み合っています。しばらくしてから再度お試しください",
                "시스템이 바쁩니다. 잠시 후 다시 시도해 주세요");
        registerMessage("系统繁忙，登录失败，请稍后重试", "system.busy.loginFailed",
                "Sign-in failed because the system is busy. Please try again later",
                "系統繁忙，登入失敗，請稍後重試",
                "系統忙碌中，登入失敗，請稍後重試",
                "システムが混み合っているためログインできませんでした。しばらくしてからもう一度お試しください",
                "시스템이 바빠 로그인하지 못했습니다. 잠시 후 다시 시도해 주세요");
        registerMessage("当前学年暂不可查询", "grade.currentYearUnavailable",
                "Grades are not available for the current academic year",
                "當前學年暫不可查詢",
                "當前學年暫時無法查詢",
                "現在の学年の成績はまだ照会できません",
                "현재 학년의 성적은 아직 조회할 수 없습니다");
        registerMessage("成绩数据更新成功", "grade.cache.updated",
                "Grade data refreshed successfully",
                "成績數據更新成功",
                "成績資料更新成功",
                "成績データを更新しました",
                "성적 데이터를 성공적으로 새로고쳤습니다");
        registerMessage("当前学年成绩缓存数据不完整，请重新查询", "grade.cacheIncomplete",
                "Cached grades for the current academic year are incomplete, please refresh and try again",
                "當前學年成績緩存數據不完整，請重新查詢",
                "當前學年成績快取資料不完整，請重新查詢",
                "現在の学年の成績キャッシュが不完全です。再度照会してください",
                "현재 학년의 성적 캐시 데이터가 완전하지 않습니다. 다시 조회해 주세요");
        registerMessage("教务系统异常", "grade.eduSystemError",
                "The academic system is temporarily unavailable",
                "教務系統異常",
                "教務系統異常",
                "教務システムで一時的なエラーが発生しました",
                "학사 시스템에 일시적인 오류가 발생했습니다");
        registerMessage("网络连接超时", "grade.networkTimeout",
                "Network timeout",
                "網絡連接超時",
                "網路連線逾時",
                "ネットワークがタイムアウトしました",
                "네트워크 연결 시간이 초과되었습니다");
        registerMessage("用户密码错误", "grade.userPasswordIncorrect",
                "Incorrect password",
                "用戶密碼錯誤",
                "使用者密碼錯誤",
                "パスワードが正しくありません",
                "비밀번호가 올바르지 않습니다");
        registerMessage("查询条件不可用", "query.conditionUnavailable",
                "Query conditions are not available",
                "查詢條件不可用",
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
                "檢測到可疑登入請求，請重新登入以完成身份驗證",
                "偵測到可疑登入請求，請重新登入以完成身分驗證",
                "不審なログインリクエストが検出されました。本人確認のため再度ログインしてください",
                "의심스러운 로그인 요청이 감지되었습니다. 본인 확인을 위해 다시 로그인해 주세요");
        registerMessage("没有对应的登录凭证记录，请尝试重新登录", "token.notFound",
                "No matching sign-in record was found, please sign in again",
                "沒有對應的登入憑證記錄，請嘗試重新登入",
                "沒有對應的登入憑證記錄，請嘗試重新登入",
                "一致するログイン記録が見つかりません。再度ログインしてください",
                "일치하는 로그인 기록이 없습니다. 다시 로그인해 주세요");
        registerMessage("登录凭证校验服务异常，请联系管理员", "token.serviceError",
                "The session validation service is unavailable, please contact the administrator",
                "登入憑證校驗服務異常，請聯繫管理員",
                "登入憑證校驗服務異常，請聯繫管理員",
                "ログイン検証サービスでエラーが発生しました。管理者にお問い合わせください",
                "로그인 검증 서비스에 오류가 발생했습니다. 관리자에게 문의해 주세요");
        registerMessage("上传的图片文件不合法", "profile.avatar.invalidFile",
                "The uploaded image file is invalid",
                "上傳的圖片檔案不合法",
                "上傳的圖片檔案不合法",
                "アップロードした画像ファイルが無効です",
                "업로드한 이미지 파일이 올바르지 않습니다");
        registerMessage("头像功能未启用", "profile.avatar.disabled",
                "Avatar upload is not enabled",
                "頭像功能未啟用",
                "頭像功能未啟用",
                "アバター機能は有効になっていません",
                "아바타 기능이 활성화되어 있지 않습니다");
        registerMessage("用户不存在或未登录", "profile.user.notFoundOrLoggedOut",
                "The user was not found or is not signed in",
                "用戶不存在或未登入",
                "使用者不存在或未登入",
                "ユーザーが見つからないか、ログインしていません",
                "사용자를 찾을 수 없거나 로그인되어 있지 않습니다");
        registerMessage("个人简介长度不合法", "profile.introduction.invalidLength",
                "The bio length is invalid",
                "個人簡介長度不合法",
                "個人簡介長度不合法",
                "自己紹介の長さが無効です",
                "자기소개 길이가 올바르지 않습니다");
        registerMessage("请求参数异常", "request.parameterInvalid",
                "Request parameters are invalid",
                "請求參數異常",
                "請求參數異常",
                "リクエストパラメータが不正です",
                "요청 파라미터가 올바르지 않습니다");
        registerMessage("不合法的国家/地区代码", "profile.location.invalidRegion",
                "Invalid country or region code",
                "不合法的國家或地區代碼",
                "不合法的國家或地區代碼",
                "国または地域コードが無効です",
                "국가 또는 지역 코드가 올바르지 않습니다");
        registerMessage("不合法的省/州代码", "profile.location.invalidState",
                "Invalid state or province code",
                "不合法的省或州代碼",
                "不合法的省或州代碼",
                "州または省コードが無効です",
                "주 또는 성 코드가 올바르지 않습니다");
        registerMessage("不合法的市/直辖市代码", "profile.location.invalidCity",
                "Invalid city code",
                "不合法的市或直轄市代碼",
                "不合法的市或直轄市代碼",
                "市コードが無効です",
                "시 코드가 올바르지 않습니다");
        registerMessage("昵称长度不合法", "profile.nickname.invalidLength",
                "The nickname length is invalid",
                "暱稱長度不合法",
                "暱稱長度不合法",
                "ニックネームの長さが無効です",
                "닉네임 길이가 올바르지 않습니다");
        registerMessage("没有空闲的课室", "spare.empty",
                "No spare classrooms are available",
                "沒有空閒的課室",
                "沒有空閒的課室",
                "空き教室はありません",
                "사용 가능한 빈 강의실이 없습니다");
        registerMessage("课表数据更新成功", "schedule.cache.updated",
                "Schedule data refreshed successfully",
                "課表數據更新成功",
                "課表資料更新成功",
                "時間割データを更新しました",
                "시간표 데이터를 성공적으로 새로고쳤습니다");
        registerMessage("添加成功", "common.addSuccess",
                "Added successfully",
                "添加成功",
                "新增成功",
                "追加しました",
                "추가했습니다");
        registerMessage("请求过于频繁，请稍后再试", "request.tooFrequent",
                "Too many requests, please try again later",
                "請求過於頻繁，請稍後再試",
                "請求過於頻繁，請稍後再試",
                "リクエストが多すぎます。しばらくしてから再度お試しください",
                "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요");
        registerMessage("不受支持的国际手机区号", "phone.areaCode.unsupported",
                "This international phone area code is not supported",
                "不受支援的國際手機區號",
                "不受支援的國際手機區號",
                "この国際電話番号の国番号には対応していません",
                "지원하지 않는 국제 전화 국가번호입니다");
        registerMessage("当前用户未绑定手机号", "phone.notBound",
                "The current user has not linked a phone number",
                "當前用戶未綁定手機號",
                "目前使用者尚未綁定手機號",
                "現在のユーザーは電話番号を連携していません",
                "현재 사용자는 전화번호를 연결하지 않았습니다");
        registerMessage("当前用户未绑定电子邮件", "email.notBound",
                "The current user has not linked an email address",
                "當前用戶未綁定電子郵件",
                "目前使用者尚未綁定電子郵件",
                "現在のユーザーはメールアドレスを連携していません",
                "현재 사용자는 이메일을 연결하지 않았습니다");
        registerMessage("24小时内已导出过用户数据，请勿重复提交请求", "userData.export.alreadySubmitted",
                "Your user data was already exported within the last 24 hours, please do not submit again",
                "24小時內已導出過用戶數據，請勿重複提交請求",
                "24小時內已匯出過使用者資料，請勿重複提交請求",
                "過去24時間以内にユーザーデータをエクスポート済みです。再度送信しないでください",
                "최근 24시간 이내에 사용자 데이터를 이미 내보냈습니다. 다시 요청하지 마세요");
        registerMessage("系统正在导出用户数据，请稍候再返回下载", "userData.export.inProgress",
                "Your user data is being prepared, please come back later to download it",
                "系統正在導出用戶數據，請稍候再返回下載",
                "系統正在匯出使用者資料，請稍後再回來下載",
                "ユーザーデータを準備中です。しばらくしてからダウンロードしてください",
                "사용자 데이터를 준비 중입니다. 잠시 후 다시 내려받아 주세요");
        registerMessage("请先提交用户数据导出请求", "userData.export.required",
                "Please submit a user data export request first",
                "請先提交用戶數據導出請求",
                "請先提交使用者資料匯出請求",
                "先にユーザーデータのエクスポート申請を送信してください",
                "먼저 사용자 데이터 내보내기 요청을 제출해 주세요");
        registerMessage("感谢您的反馈", "feedback.received",
                "Thanks for your feedback",
                "感謝您的反饋",
                "感謝您的回饋",
                "フィードバックありがとうございます",
                "피드백 감사합니다");
        registerMessage("不合法的Cron请求", "cron.invalidRequest",
                "Invalid cron request",
                "不合法的 Cron 請求",
                "不合法的 Cron 請求",
                "無効な Cron リクエストです",
                "잘못된 Cron 요청입니다");
        registerMessage("不合法的图片文件", "upload.invalidImage",
                "Invalid image file",
                "不合法的圖片檔案",
                "不合法的圖片檔案",
                "無効な画像ファイルです",
                "잘못된 이미지 파일입니다");
        registerMessage("不支持混合上传图片参数", "upload.mixedImageParamsUnsupported",
                "Mixed image upload parameters are not supported",
                "不支援混合上傳圖片參數",
                "不支援混合上傳圖片參數",
                "画像アップロードの混在パラメータには対応していません",
                "혼합된 이미지 업로드 파라미터는 지원하지 않습니다");
        registerMessage("上传失败", "upload.failed",
                "Upload failed",
                "上傳失敗",
                "上傳失敗",
                "アップロードに失敗しました",
                "업로드에 실패했습니다");
        registerMessage("获取二手交易商品预览图失败", "marketplace.preview.failed",
                "Failed to load the marketplace item preview image",
                "獲取二手交易商品預覽圖失敗",
                "取得二手交易商品預覽圖失敗",
                "中古商品プレビュー画像の取得に失敗しました",
                "중고거래 상품 미리보기 이미지를 불러오지 못했습니다");
        registerMessage("已下架的二手交易信息不能查看", "marketplace.offUnavailable",
                "Items that have been taken down cannot be viewed",
                "已下架的二手交易資訊不能查看",
                "已下架的二手交易資訊不能查看",
                "掲載終了した中古商品情報は閲覧できません",
                "내려간 중고거래 정보는 볼 수 없습니다");
        registerMessage("已出售的二手交易信息不能查看", "marketplace.soldUnavailable",
                "Sold marketplace items cannot be viewed",
                "已出售的二手交易資訊不能查看",
                "已出售的二手交易資訊不能查看",
                "販売済みの中古商品情報は閲覧できません",
                "판매 완료된 중고거래 정보는 볼 수 없습니다");
        registerMessage("商品价格不合法", "marketplace.invalidPrice",
                "Invalid item price",
                "商品價格不合法",
                "商品價格不合法",
                "商品価格が無効です",
                "상품 가격이 올바르지 않습니다");
        registerMessage("话题图片上传失败", "topic.imageUpload.failed",
                "Failed to upload topic images",
                "話題圖片上傳失敗",
                "話題圖片上傳失敗",
                "トピック画像のアップロードに失敗しました",
                "토픽 이미지 업로드에 실패했습니다");
        registerMessage("树洞信息不能为空", "secret.content.required",
                "Secret content cannot be empty",
                "樹洞資訊不能為空",
                "樹洞資訊不能為空",
                "シークレット投稿の内容は空にできません",
                "비밀글 내용은 비워 둘 수 없습니다");
        registerMessage("语音内容不能为空", "secret.voice.required",
                "Voice content cannot be empty",
                "語音內容不能為空",
                "語音內容不能為空",
                "音声内容は空にできません",
                "음성 내용은 비워 둘 수 없습니다");
        registerMessage("语音文件大小过大", "secret.voice.tooLarge",
                "The voice file is too large",
                "語音檔案過大",
                "語音檔案過大",
                "音声ファイルが大きすぎます",
                "음성 파일이 너무 큽니다");
        registerMessage("语音上传失败", "secret.voice.uploadFailed",
                "Failed to upload the voice message",
                "語音上傳失敗",
                "語音上傳失敗",
                "音声アップロードに失敗しました",
                "음성 업로드에 실패했습니다");
        registerMessage("树洞信息类型不合法", "secret.type.invalid",
                "Invalid secret post type",
                "樹洞資訊類型不合法",
                "樹洞資訊類型不合法",
                "シークレット投稿の種類が無効です",
                "비밀글 유형이 올바르지 않습니다");
        registerMessage("查询的校园树洞信息不存在", "secret.notFound",
                "The requested secret post does not exist",
                "查詢的校園樹洞資訊不存在",
                "查詢的校園樹洞資訊不存在",
                "指定したシークレット投稿は存在しません",
                "조회한 비밀글이 존재하지 않습니다");
        registerMessage("文本内容超过限制", "content.textTooLong",
                "Text content exceeds the limit",
                "文本內容超過限制",
                "文本內容超過限制",
                "テキスト内容が上限を超えています",
                "텍스트 내용이 제한을 초과했습니다");
        registerMessage("拍好校园图片上传失败", "photograph.uploadFailed",
                "Failed to upload campus photos",
                "拍好校園圖片上傳失敗",
                "拍好校園圖片上傳失敗",
                "キャンパス写真のアップロードに失敗しました",
                "캠퍼스 사진 업로드에 실패했습니다");
        registerMessage("校园新闻站点访问失败", "news.siteAccess.failed",
                "The campus news site is temporarily unavailable",
                "校園新聞站點訪問失敗",
                "校園新聞站點訪問失敗",
                "キャンパスニュースサイトに一時的にアクセスできません",
                "캠퍼스 뉴스 사이트에 일시적으로 접근할 수 없습니다");
        registerMessage("教务系统页面结构异常，未找到学期下拉框", "edu.pageStructure.termSelectMissing",
                "The academic system page structure has changed and the term selector could not be found",
                "教務系統頁面結構異常，未找到學期下拉框",
                "教務系統頁面結構異常，未找到學期下拉選單",
                "教務システムのページ構成が変更され、学期セレクターが見つかりませんでした",
                "학사 시스템 페이지 구조가 바뀌어 학기 선택 항목을 찾지 못했습니다");
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
                "zh-HK", "{0} 評論了你的樹洞：{1}",
                "zh-TW", "{0} 評論了你的樹洞：{1}",
                "en", "{0} commented on your confession: {1}",
                "ja", "{0} さんがあなたのツリーホールにコメントしました: {1}",
                "ko", "{0}님이 당신의 트리홀에 댓글을 남겼습니다: {1}"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.secret.like.content", Map.of(
                "zh-CN", "{0} 点赞了你的树洞",
                "zh-HK", "{0} 點讚了你的樹洞",
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
                "zh-HK", "表白牆有人參與猜名字",
                "zh-TW", "表白牆有人參與猜名字",
                "en", "Someone joined your guess-the-name interaction",
                "ja", "告白ウォールの名前当てに参加した人がいます",
                "ko", "누군가 이름 맞히기 활동에 참여했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.comment.contentWithDetail", Map.of(
                "zh-CN", "{0} 评论了你的表白：{1}",
                "zh-HK", "{0} 評論了你的表白：{1}",
                "zh-TW", "{0} 評論了你的表白：{1}",
                "en", "{0} commented on your confession wall post: {1}",
                "ja", "{0} さんがあなたの告白にコメントしました: {1}",
                "ko", "{0}님이 당신의 고백 글에 댓글을 남겼습니다: {1}"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.like.content", Map.of(
                "zh-CN", "{0} 点赞了你的表白",
                "zh-HK", "{0} 點讚了你的表白",
                "zh-TW", "{0} 按讚了你的表白",
                "en", "{0} liked your confession wall post",
                "ja", "{0} さんがあなたの告白にいいねしました",
                "ko", "{0}님이 당신의 고백 글을 좋아합니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.guess.correct", Map.of(
                "zh-CN", "{0} 猜中了你的表白对象",
                "zh-HK", "{0} 猜中了你的表白對象",
                "zh-TW", "{0} 猜中了你的表白對象",
                "en", "{0} guessed your crush correctly",
                "ja", "{0} さんがあなたの想い人を正しく当てました",
                "ko", "{0}님이 당신의 짝사랑 상대를 맞혔습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.express.guess.joined", Map.of(
                "zh-CN", "{0} 参与了你的猜名字",
                "zh-HK", "{0} 參與了你的猜名字",
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
                "zh-HK", "{0} 點讚了你的話題",
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
                "zh-HK", "{0} 評論了你的作品：{1}",
                "zh-TW", "{0} 評論了你的作品：{1}",
                "en", "{0} commented on your post: {1}",
                "ja", "{0} さんがあなたの作品にコメントしました: {1}",
                "ko", "{0}님이 당신의 작품에 댓글을 남겼습니다: {1}"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.photograph.like.content", Map.of(
                "zh-CN", "{0} 点赞了你的作品",
                "zh-HK", "{0} 點讚了你的作品",
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
                "zh-HK", "撩一下已通過",
                "zh-TW", "撩一下已通過",
                "en", "Your poke was accepted",
                "ja", "アプローチが承認されました",
                "ko", "찔러보기가 수락되었습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.rejected.title", Map.of(
                "zh-CN", "撩一下未通过",
                "zh-HK", "撩一下未通過",
                "zh-TW", "撩一下未通過",
                "en", "Your poke was declined",
                "ja", "アプローチは承認されませんでした",
                "ko", "찔러보기가 거절되었습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.received.content", Map.of(
                "zh-CN", "{0} 向你发起了撩一下",
                "zh-HK", "{0} 向你發起了撩一下",
                "zh-TW", "{0} 向你發起了撩一下",
                "en", "{0} sent you a poke",
                "ja", "{0} さんがあなたにアプローチしました",
                "ko", "{0}님이 당신에게 찔러보기를 보냈습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.received.contentWithDetail", Map.of(
                "zh-CN", "{0} 向你发起了撩一下：{1}",
                "zh-HK", "{0} 向你發起了撩一下：{1}",
                "zh-TW", "{0} 向你發起了撩一下：{1}",
                "en", "{0} sent you a poke: {1}",
                "ja", "{0} さんがあなたにアプローチしました: {1}",
                "ko", "{0}님이 당신에게 찔러보기를 보냈습니다: {1}"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.accepted.content", Map.of(
                "zh-CN", "{0} 通过了你的请求",
                "zh-HK", "{0} 通過了你的請求",
                "zh-TW", "{0} 通過了你的請求",
                "en", "{0} accepted your request",
                "ja", "{0} さんがあなたのリクエストを承認しました",
                "ko", "{0}님이 당신의 요청을 수락했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.dating.rejected.content", Map.of(
                "zh-CN", "{0} 拒绝了你的请求",
                "zh-HK", "{0} 拒絕了你的請求",
                "zh-TW", "{0} 拒絕了你的請求",
                "en", "{0} declined your request",
                "ja", "{0} さんがあなたのリクエストを辞退しました",
                "ko", "{0}님이 당신의 요청을 거절했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.accepted.title", Map.of(
                "zh-CN", "订单已被接单",
                "zh-HK", "訂單已被接單",
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
                "zh-HK", "{0} 接取了你的訂單",
                "zh-TW", "{0} 接取了你的訂單",
                "en", "{0} accepted your order",
                "ja", "{0} さんがあなたの注文を受けました",
                "ko", "{0}님이 당신의 주문을 접수했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.accepted.contentWithCompany", Map.of(
                "zh-CN", "{0} 接取了你发布的 {1} 订单",
                "zh-HK", "{0} 接取了你發佈的 {1} 訂單",
                "zh-TW", "{0} 接取了你發布的 {1} 訂單",
                "en", "{0} accepted your {1} order",
                "ja", "{0} さんがあなたの {1} 注文を受けました",
                "ko", "{0}님이 당신의 {1} 주문을 접수했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.finished.content", Map.of(
                "zh-CN", "{0} 确认你的订单已完成",
                "zh-HK", "{0} 確認你的訂單已完成",
                "zh-TW", "{0} 確認你的訂單已完成",
                "en", "{0} confirmed your order was completed",
                "ja", "{0} さんがあなたの注文完了を確認しました",
                "ko", "{0}님이 당신의 주문 완료를 확인했습니다"
        ));
        MESSAGE_TRANSLATIONS.put("interaction.delivery.finished.contentWithCompany", Map.of(
                "zh-CN", "{0} 已确认你接取的 {1} 订单完成",
                "zh-HK", "{0} 已確認你接取的 {1} 訂單完成",
                "zh-TW", "{0} 已確認你接取的 {1} 訂單完成",
                "en", "{0} confirmed your {1} order was completed",
                "ja", "{0} さんがあなたの {1} 注文完了を確認しました",
                "ko", "{0}님이 당신의 {1} 주문 완료를 확인했습니다"
        ));
    }
}
