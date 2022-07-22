# CloudTips SDK for Android 

CloudTips SDK позволяет интегрировать прием чаевых в мобильные приложение для платформы Android.

### Требования
Для работы CloudTips SDK необходим Android версии 4.4 (API level 19) или выше.

### Подключение
Для подключения SDK в build.gradle уровня проекта добавить репозитрий Jitpack:

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
И в build.gradle уровня приложения доюавить зависимость:

```
dependencies {
	implementation 'com.github.cloudpayments:CloudTips-SDK-Android:1.1.4'
}
```

### Google Pay

CloudTips SDK поддерживает Google Pay.
Чтобы включить Google Pay в приложении, добавьте следующие метаданные в тег <application> файла AndroidManifest.xml.

```
<meta-data
    android:name="com.google.android.gms.wallet.api.enabled"
    android:value="true" />
```

### Yandex Pay

CloudTips SDK поддерживает Yandex Pay.
Чтобы включить Yandex Pay в приложении, добавьте в build.gradle:

```
defaultConfig { 
	...
	manifestPlaceholders = [YANDEX_CLIENT_ID: "ID взять из админки Яндекса https://oauth.yandex.ru/"]
	...
}
```

Ключи подписи и название пакета добавить в админку Яндекса в необходимые поля: SHA256 Fingerprints и Android package name

### Структура проекта:

* **app** - Пример реализации приложения с использованием SDK
* **sdk** - Исходный код SDK

### Использование

Создайте объект TipsData с параметрами номер телефона (в формате +7**********) и имя пользователя (если пользователя с таким номером телефона нет в системе CloudTips, то будет зарегистрирован новый пользователь с этим именем), затем передайте его в объект TipsConfiguration и запустите SDK. 

Если вы являетесь партнером CloudTips, передайте в конфигурацию id партнера.

```
val tipsData = TipsData(phone, "User name", "Partner ID")
val configuration = TipsConfiguration(tipsData) 
val configuration = TipsConfiguration(tipsData, true) // Если необходимо включить режим тестирования
CloudTipsSDK.getInstance().start(configuration, this)
```

### Поддержка

По возникающим вопросам техничечкого характера обращайтесь на support@cloudpayments.ru
