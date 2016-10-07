# HCI Debugger

[![CircleCI](https://img.shields.io/circleci/project/akinaru/hci-debugger.svg?maxAge=2592000?style=plastic)](https://circleci.com/gh/akinaru/hci-debugger)
[![License](http://badge.kloud51.com/pypi/l/html2text.svg)](LICENSE.md)

Android application used to decode & display HCI packets

[![Download RFdroid from Google Play](http://www.android.com/images/brand/android_app_on_play_large.png)](https://play.google.com/store/apps/details?id=com.github.akinaru.hcidebugger)

![screenshot](img/screen.gif)

In order to use this application, you have to activate Bluetooth snoop HCI log from developper options

### <a href="CHANGELOG.md">watch change logs</a>

## Build

### Get source code

```
git clone git@github.com:akinaru/hci-debugger.git
cd hci-debugger
git submodule update --init --recursive
```

### Build Android App

```
./gradlew clean build
```

## Tested devices

* Samsung Galaxy S4
* Samsung Galaxy S6

## External projects

* btsnoop-decoder : https://github.com/akinaru/btsnoop-decoder

* bluetooth-hci-decoder : https://github.com/akinaru/bluetooth-hci-decoder

## License

```
Copyright (C) 2016  Bertrand Martel

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

HCI Debugger is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with HCI Debugger.  If not, see <http://www.gnu.org/licenses/>.
```