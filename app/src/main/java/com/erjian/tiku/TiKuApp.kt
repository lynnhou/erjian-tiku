package com.erjian.tiku

import android.app.Application
import com.erjian.tiku.data.UserStore

class TiKuApp : Application() {
    val userStore: UserStore by lazy { UserStore(this) }
}
