# sdk-android
Android SDK и демо-приложение для нового VisitorAPI

**Подключение SDK**

todo: описать

**Настройка**

Для начала нужно инициализировать обьект LiveTex.
Делается это в Application классе
([например App.java](demo/src/main/java/ru/livetex/demoapp/App.java)).

`		new LiveTex.Builder(Const.HOST, Const.TOUCHPOINT).build();`

Укажите адрес сервера (будет зашито или опционально) и Touchpoint
(берется в личном кабинете)

**Использование**

Пример использования библиотеки можно посмотреть в
[демо приложении](demo/).  
todo: описать подробно

