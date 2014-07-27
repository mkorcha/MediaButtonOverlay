MediaButtonOverlay
==================

An Android application that provides a persisting overlay of media buttons for quick access to changing music.

App can be found on [Google Play](https://play.google.com/store/apps/details?id=com.mikekorcha.mediabuttonoverlay)

Latest Update
-------------
Haptic feedback and brightening button options have been added:

* Haptic feedback: when you tap a button, a smalll vibration will occur in response

* Brightening buttons: when you tap a button, it will brighten briefly in response


Notes
-----

Project built in Android Studio. You should just be able to open the project from there. 

Omitted is an XML file in res/values called sensitive.xml. This is for the In-App Billing part, which handles a donation. It contains two strings: license\_key and donation\_sku. These should be relatively straightforward to add.

Credit
------

* SeekBar preference: [robobunny](http://robobunny.com/wp/2013/08/24/android-seekbar-preference-v2/)
* Colored theme stuff: [android-holo-colors](http://android-holo-colors.com/)
* Icons: [iconmonstr](http://iconmonstr.com/)
* Base Tasker stuff: [Tasker](http://tasker.dinglisch.net/developers.html)
* Locale API: [Base](http://www.twofortyfouram.com/developer.html), [Android Studio Build](https://github.com/mkorcha/AndroidStudio-LocaleAPI)

License
-------

Code written specifically for this app is released under the LGPL 2.1 license. SeekBar preference was released into public domain. Locale's API was released under the Apache 2.0 license. 