# YuuPlayer

[![Release](https://jitpack.io/v/jitpack/android-example.svg)](https://jitpack.io/#jitpack/android-example)

Youtube Player without any dependency and without SDK, webview based. Control video and get video information programatically.

## How to use

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'com.github.jitpack:android-example:{latest version}'
}
```

## Basic Example
Add in your layout
```xml
<id.agusibrahim.yuuplayer.YuuPlayer
		android:layout_width="match_parent"
		android:layout_height="250dp"
		app:video_id="YOUTUBE-VIDEO-ID"
		app:autoplay="false"
		app:video_quality="small"
		android:id="@+id/mainYuuPlayer"/>
```
For more details about API, check these example
http://github.com/agusibrahim/blablabla
<img src="https://raw.githubusercontent.com/agusibrahim/YuuPlayer/master/img/Screenshot_20170715-112651.png" width="300">

## Issues
Not work on some Android API version, especially who not supporting WebView HTML5 Video.


## Contributing & license
Any contribution in order to make this library better will be welcome!

The library is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
