MediaButtonOverlay
==================

An Android application that provides a persisting overlay of media buttons for quick access to changing music.

App can be found on [Google Play](https://play.google.com/store/apps/details?id=com.mikekorcha.mediabuttonoverlay)

Copyright © Mike Korcha 2013-2015

Notes
-----

Project built in Android Studio. You should just be able to open the project from there. 

Contributing
------------

The latest release has made it significantly easier to add a player or a skin.

### Player

Extend the `MediaPlayer` class, included in the base directory, and put it in the `players` package. Then, just add the name of the player to `players_strings` and the class name to `players_classes` in the `players.xml` value file.

To allow for the button to change properly with play status, when calling the parent constructor, pass the needed `BroadcastReceiver` and `IntentFilter`.

To allow the player to start with the overlay, make sure to set `playerPackage` to the package ID of the player you want to start.

### Skin
 
Create an XML layout file. Make sure the buttons have the ID of `btnPrevious`, `btnPlayPause`, and `btnNext`, for those you want to implement. Then, add the skin name to `skins_names` and the layout resource name to `skins_resources` in the `skins.xml` value file.

To allow the orientation of the layout to change (assuming a `LinearLayout`), make sure the enclosing layout has the ID `buttonLayout`.

To allow the element to be recoloured, make sure the element is tagged "recolourable". If any additional processing needs to be done for your recolour, look at `MediaOverlayView.java` in the `setColour()` method.

License
-------

LGPL 2.1. All libraries are licensed as stated by their authors.

Credit
------

* [SliderPreference](https://github.com/jayschwa/AndroidSliderPreference)
* [ColorPickerPreference](https://github.com/attenzione/android-ColorPickerPreference)
* [FloatingActionButton](https://github.com/futuresimple/android-floating-action-button)
* [Material Design Colors](https://github.com/wada811/Android-Material-Design-Colors)
* [Icons](http://iconmonstr.com/)