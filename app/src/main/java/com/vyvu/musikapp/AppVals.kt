package com.vyvu.musikapp

abstract class AppVals {
    abstract class Other {
        companion object {
            const val MILI_TO_SEC: Long = 1000
        }
    }

    abstract class Code {
        companion object {
            const val NONE_CODE = -1
        }
    }

    abstract class String {
        companion object {
            const val EMPTY_STRING = ""
            const val SLASH_STRING = "/"
            const val EXTENSION_MP3 = ".mp3"
            const val APP_NAME = "Musik"
            const val MUSIK_SERVICE_NAME = "MusikService"
        }
    }

    abstract class Action {
        companion object {
            const val INTENT_KEY_ACTIONS_CONTROL = "Action.Key"
            const val STOP = "Stop"
            const val LOOP_SINGLE = "Loop single"
            const val OFF_LOOP_SINGLE = "Off loop single"
            const val PLAY_STREAM = "Play stream"
            const val INIT_PLAYLIST = "Init playlist"
            const val PLAY_PLAYLIST = "Play playlist"
            const val PAUSE_PLAYLIST = "Pause playlist"
            const val RESUME_PLAYLIST = "Resume playlist"
            const val NEXT_PLAYLIST = "Next playlist"
            const val PREVIOUS_PLAYLIST = "Previous playlist"
            const val LOOP_ALL_PLAYLIST = "Loop all playlist"
            const val SHUFFLE_PLAYLIST = "Shuffle playlist"
            const val OFF_SHUFFLE_PLAYLIST = "Off shuffle playlist"
            const val OFF_LOOP_ALL_PLAYLIST = "Off loop"
            const val STOP_SERVICE = "Stop service"
            const val SEEK_PLAYLIST = "Seek"
            const val DECLARE_CURRENT_POS = "Declare current pos"

            const val STOP_CODE = 0
            const val LOOP_SINGLE_CODE = 1
            const val OFF_LOOP_SINGLE_CODE = 1
            const val PAUSE_PLAYLIST_CODE = 3
            const val RESUME_PLAYLIST_CODE = 3
            const val NEXT_PLAYLIST_CODE = 4
            const val PREVIOUS_PLAYLIST_CODE = 5
            const val LOOP_ALL_PLAYLIST_CODE = 6
            const val SHUFFLE_PLAYLIST_CODE = 7
            const val OFF_SHUFFLE_PLAYLIST_CODE = 7
            const val OFF_LOOP_ALL_PLAYLIST_CODE = 6
            const val STOP_SERVICE_CODE = 8
        }
    }
}
