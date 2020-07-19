# Android mobile SDK and demo app
LiveTex Mobile SDK предоставляет набор инструментов для организации консультирования пользователей мобильных приложений.  
Механизм взаимодействия с сервером основывается на новом Visitor API.

**Демо**

Пример использования библиотеки можно посмотреть вживую в полноценном
демо приложении, поставив его на устройство через официальный [Google Play](https://play.google.com/store/apps/details?id=ru.livetex.demoapp).

Исходники этого демо приложения лежат в папке [demo](demo/).

Подключение SDK
===============
[![Release](https://jitpack.io/v/LiveTex/sdk-android.svg)](https://jitpack.io/#LiveTex/sdk-android)

В build.gradle (который в корне проекта) добавить репозиторий jitpack

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

В build.gradle (который в модуле приложения) добавить зависимость SDK
актуальной версии (см.
[Releases](https://github.com/LiveTex/sdk-android/releases))

	dependencies {
	        implementation 'com.github.LiveTex:sdk-android:x.y'
	}

Настройка
=========
Для начала нужно инициализировать обьект LiveTex.
Делается это обычно в Application классе
([например App.java](demo/src/main/java/ru/livetex/demoapp/App.java)).

`		new LiveTex.Builder(Const.TOUCHPOINT).build();`

Укажите Touchpoint (берется в личном кабинете).

Далее можно обращаться к синглтону через LiveTex.getInstance().

**Пуши**

В демо приложении есть пример того, как подключить пуши и передать токен в LiveTex.
Для подключения пушей нужно сначала подключить Firebase Messaging Service по [их стандартной инструкции](https://firebase.google.com/docs/cloud-messaging/android/client).
С помощью функции FirebaseInstanceId.getInstance().getInstanceId() нужно зарегистрировать устройство в Firebase и получить в ответ device token, который в свою очередь нужно передать в билдер LiveTex. Это несинхронная операция которая требует какое-то время, поэтому функция реактивная.

**Важно** - функция initLiveTex() должна быть вызвана до использования класса LiveTex. Поэтому инициализировать его в случае с Firebase стоит заранее (в [SplashActivity](demo/src/main/java/ru/livetex/demoapp/ui/splash/SplashActivity.java) например).

	public Completable init() {
		return Completable.create(emitter -> {
			FirebaseInstanceId.getInstance().getInstanceId()
					.addOnCompleteListener(task -> {
						if (!task.isSuccessful()) {
							Log.w(TAG, "getInstanceId failed", task.getException());
							initLiveTex();
							emitter.onComplete();
							return;
						}

						String token = task.getResult().getToken();
						Log.i(TAG, "firebase token = " + token);

						initLiveTex();
						emitter.onComplete();
					});
		});
	}

	private void initLiveTex() {
		new LiveTex.Builder(Const.TOUCHPOINT)
				.setDeviceToken(FirebaseInstanceId.getInstance().getToken())
				.build();
	}

Использование
=============

**Основные классы**

[LiveTex](sdk/src/main/java/ru/livetex/sdk/LiveTex.java) - класс для настройки и получения доступа к компонентам.

[NetworkManager](sdk/src/main/java/ru/livetex/sdk/network/NetworkManager.java) - класс для работы с сетевым операциями, в том числе авторизация и подключение к вебосокету чата.

[LiveTexMessagesHandler](sdk/src/main/java/ru/livetex/sdk/logic/LiveTexMessagesHandler.java) - класс для работы с логикой общения по вебсокету. Обработка входящих и исходящих событий.

**Авторизация и подключение**

Для авторизации и подключения используется один метод [NetworkManager.connect()](sdk/src/main/java/ru/livetex/sdk/network/NetworkManager.java). В него передается параметры visitorToken и authTokenType.

visitorToken - токен (id) пользователя (который получили раньше или null если первая авторизация). Если у вас в системе уже есть сущность пользователя с неким айди, можете использовать его с соответствующим authTokenType;

authTokenType - тип авторизации, обычный (LiveTex система токенов) или кастомный (ваша система токенов).

В ответ система выдаст токен (сгенерирует если вы подали null или тот же самый с которым вы зашли). Если вы пользуетесь своей системой токенов, можете игнорировать ответ.

Для того, чтобы отключиться (и отключить автоматическое восстановление вебсокета) используйте метод forceDisconnect().

**События в чате**

Вся логика чата построена на обмене событиями (которые проходят по вебсокету) в классе [LiveTexMessagesHandler](sdk/src/main/java/ru/livetex/sdk/logic/LiveTexMessagesHandler.java)

**Входящие события**

Со стороны экрана чата нужно подписаться на входящие события (пример есть в [ChatViewModel.subscribe())](demo/src/main/java/ru/livetex/demoapp/ui/chat/ChatViewModel.java)

dialogStateUpdate() - обновление состояния диалога (здесь есть информация об операторе)

historyUpdate() - обновление истории диалога. Представляет собой отрезок сообщений, то есть могут прийти как и следующие сообщения, так и прошлые (при запросе предыдущей истории соответственно).

attributesRequest() - событие запроса аттрибутов. Для корректной логики на него **обязательно** нужно ответить sendAttributes().

departmentRequest() - событие запроса департамента (комнаты чата). Для корректной логики на него **обязательно** нужно ответить sendDepartmentSelectionEvent().

employeeTyping() - событие набора текста оператором.

**Исходящие события**

sendTextMessage(text) - отправка обычного текстового сообщения.

sendFileMessage(FileUploadedResponse) - отправка файла (картинка, документ и прочее). Перед отправкой файл нужно загрузить через ApiManager (смотрите пример в [ChatViewModel.sendFileMessage())](demo/src/main/java/ru/livetex/demoapp/ui/chat/ChatViewModel.java))

sendRatingEvent(isPositiveFeedback) - оценка качества диалога.

sendDepartmentSelectionEvent(departmentId) - выбор департамента (команты чата), ответ на departmentRequest().

sendAttributes(...) - отправка свойств пользователя, ответ на attributesRequest(). Набор обязательных и опциональных полей зависит от проекта.

sendTypingEvent(text) - отправка уведомления о том, что пользователь печатает текст.

getHistory(messageId) - получение предыстории сообщений, которые идут до указанного. Ответ придет в historyUpdate(), но здесь в подписке вы получите количество предыдущих сообщений. 0 означает что все сообщения загружены.

sendButtonPressedEvent(payload) - отправка нажатия на экшн кнопку (которая внутри входящего сообщения бота).