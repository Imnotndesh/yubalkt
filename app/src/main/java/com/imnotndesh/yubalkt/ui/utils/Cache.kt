package com.imnotndesh.yubalkt.ui.utils

import com.imnotndesh.yubalkt.core.network.LogEntry
import com.imnotndesh.yubalkt.core.network.SubscriptionResponse
import com.imnotndesh.yubalkt.core.network.YubalJob

object DataCache {
    var jobs: List<YubalJob> = emptyList()
    var subscriptions: List<SubscriptionResponse> = emptyList()
    var logs: List<LogEntry> = emptyList()
    var jobsLoaded = false
    var subscriptionsLoaded = false
}
