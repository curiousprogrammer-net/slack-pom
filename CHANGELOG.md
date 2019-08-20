# Change Log
All notable changes to this project will be documented in this file. 
This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

## 0.1.1-SNAPSHOT

### Added
- Add `start-break` shortcut (Ctrl-Alt-Cmd-/). This is just quick shortcut for starting 5-min "pomodoro" aka "break". For other durations you can use `start-pom` directly.


## 0.1.0-SNAPSHOT

### Added 
- [Simple sound alert](https://freesound.org/people/themusicalnomad/sounds/253886/) when pomodoro is stopped.

### Fixed
- Fixed frequent application crashes caused by jnativehook library used for registering global keyboard shortcuts.
[JKeyMaster](https://github.com/tulskiy/jkeymaster) is now used instead.



[Unreleased]: https://github.com/your-name/change/compare/0.1.0...HEAD
