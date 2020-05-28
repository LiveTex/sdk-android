# sdk-android
Android SDK и демо-приложение для нового VisitorAPI

**Подключение SDK**  
[![](https://jitpack.io/v/LiveTex/sdk-android.svg)](https://jitpack.io/#LiveTex/sdk-android)

В build.gradle (который в корне проекта) добавить репозиторий jitpack

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

В build.gradle (который в модуле приложения) добавить зависимость SDK
актуальной версии

	dependencies {
	        implementation 'com.github.LiveTex:sdk-android:x.y'
	}

**Настройка**

Для начала нужно инициализировать обьект LiveTex.
Делается это в Application классе
([например App.java](demo/src/main/java/ru/livetex/demoapp/App.java)).

`		new LiveTex.Builder(Const.TOUCHPOINT).build();`

Укажите Touchpoint
(берется в личном кабинете)

**Использование**

Пример базового использования библиотеки можно посмотреть в
[демо приложении](demo/).

