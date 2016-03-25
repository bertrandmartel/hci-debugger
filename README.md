# HCI Debugger

Android application used to decode & display HCI packets

![screenshot](img/screen.gif)

In order to use this application, you have to activate Bluetooth snoop HCI log from developper options

## Build

### Get code source

```
git clone git@github.com:akinaru/hci-debugger.git
cd hci-debugger
git submodule update --init --recursive
```

### Build Android App

```
./gradlew clean build
```

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

Foobar is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
```