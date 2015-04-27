MediaButtonOverlay
==================

An Android application that provides a persisting overlay of media buttons for quick access to changing music.

App can be found on [Google Play](https://play.google.com/store/apps/details?id=com.mikekorcha.mediabuttonoverlay)

Copyright © Mike Korcha 2013-2015

Notes
-----

Project built in Android Studio. First you should run the task gitClone (using gradlew) to ensure that you pull in the other projects this one depends on. Afterwards, you should just be able to build and run the project as usual.

Use
---

### Basics

All you really have to do to get started is select your media player from the menu, then hit the red start button to get started. You can opt to start the player with the overlay, in case it hasn't started already.

If your player isn't shown, the "Default Fallback" option is usually sufficient - if not, see below on how to add your own, or alternatively, open an issue.

### Customizing

You can set the skin, the colour (if the skin allows for it), the opacity (transparency) of the overlay on the screen, and set if it should display horizontally or vertically, for skins which allow it.

Want to create a skin? See below for more information.

### The Overlay

The overlay can be moved to either side of the screen by long pressing, then dragging it to either side of the screen. When holding it over a droppable area, it should be lit up. You can also move vertically along these areas to reposition the overlay to your liking.

There is also an "x" icon that shows. Simply drag the overlay onto this icon to stop it. Alternatively, you can press the red start button again in the menu.

Note: If the overlay drop view (for lack of a better name) gets stuck, you can tap in the same locations that you would be able to drop to get the same functionality.

### Automation

Media Button Overlay comes with a few automation features built in to start and stop the overlay via Intents, which can be used from your own applications or other methods.

To start the overlay (using the settings previously selected):

```
com.mikekorcha.mediabuttonoverlay.START
```

To stop the overlay:

```
com.mikekorcha.mediabuttonoverlay.STOP
```

Additionally, this app has built-in support for Tasker and Locale, so if you use either of these automation apps you don't need to do anything else.

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
