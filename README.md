# slack-pom

Update your slack status automatically when using pomodoro technique.

## Features

Simple command line application that can start/stop pomodoro session, show pomodoro timer
as a system tray icon as well as a transparent overlay window in the top right corner,
and update slack profile status.

### System Tray Icon

Once the pomodoro session is started, you'll see a pomodoro timer in the "system tray"
(the menu bar at the very top of the screen on my Max OS):

![system tray icon](resources/system-tray-icon.png)

**Gotcha**: 
If you have many apps with a system tray icon, it can happen that you won't see 
the pomodoro timer in the system tray because there's not enough place for it.
This is the primary reason why there's an complementary visualization method using overlay window.
Check [Seeing All Icons on the Menu Bar](https://apple.stackexchange.com/questions/145187/seeing-all-icons-on-the-menu-bar).

### Overlay Window in the Top Right Screen corner

In addition to the system tray icon, the pomodoro timer with a minute granularity is shown
in the top right screen corner:

![overlway window in the top right corner](resources/overlay-window.png)

### Slack Profile Status Update

Once the pomodoro session is started you should see your slack profile status updated
with a nice tomato icon:

![slack profile status update](resources/slack-profile-status.png)

## Configuration

You need to provide a proper slack API token for your slack team.
See https://github.com/julienXX/clj-slack#usage for instructions how to get the API token.

Once you have the token, create custom [`config.edn`](resources/config.edn) file in the root directory (the directory
where the program is run).

You don't have to modify [built-in `config.edn` file](resources/config.edn).
This is used as a template and also to provide defaults for optional keys.
Just copy it and provide the required config.


## Usage

WARNING: the application has only been tested on Mac OS High Sierra 10.13.3.

### Build and Run

```
lein uberjar
java -jar target/uberjar/slack-pom-0.1.0-SNAPSHOT-standalone.jar
```

Alternatively, you can run `lein repl` and call functions from `slack-pom.core` namespace directly.

### Global Keyboard Shortcuts

When application is started, it tries to register native global hook for keyboard shortcuts
using [jnativehook](https://github.com/kwhat/jnativehook/wiki/Keyboard) library.
That means you usually need to add necessary permissions.
On Mac OS this is done in `System Preferences -> Security & Privacy -> Accessibility`.
System should show a dialog asking for such a permission during the first application run.
Add the required permission and restart the application.

Following shortcuts are registered automatically for you:

* Ctrl + Alt + Cmd + ,   (start pomodoro session with default duration)
* Ctrl + Alt + Cmd + .   (stop pomodoro)

Note that `Cmd` key is also known as `Meta` in `jnativehook` terminology.

### Using Command-Line Interface

Although the global keyboard shortcuts are the easiest way to interact with the application,
you can also interact with it through the cmd interface. 
The help is printed when the application is started:

```
Hello!
   Commands
     sp [duration-in-minutes]:    start pomodoro    - keyboard shortcut [CTRL + ALT + CMD (meta) + ,]
     tp:                          stop pomodoro     - keyboard shortcut [CTRL + ALT + CMD (meta) + .]
     h:                           help
     q:                           quit
```


## License

Copyright © 2018 Juraj Martinka (curiousprogrammer.net)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
