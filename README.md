# CloudTips SDK for Android

CloudTips SDK позволяет интегрировать прием чаевых в мобильные приложение для платформы Android.

### Требования
Для работы CloudTips SDK необходим Android версии 6 (API level 23) или выше.

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

И в build.gradle уровня приложения добавить зависимость:

```
dependencies {
	implementation 'com.github.cloudpayments:CloudTips-SDK-Android:2.0.1'
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

Создайте инстанс объекта TipsManager

```
val tipsManager = TipsManager.getInstance(context)
```

Для запуска оплаты чаевых

```
tipsManager.launch(layoutId)
, где layoutId - идентификатор визитки CloudTips
```

### Поддержка

По возникающим вопросам техничечкого характера обращайтесь на support@cp.ru
