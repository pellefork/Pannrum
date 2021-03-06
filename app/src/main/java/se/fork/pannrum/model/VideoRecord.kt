package se.fork.pannrum.model

import java.util.*


data class VideoRecord(
        var key: String? = null,
        var videoRec: VideoRec? = null
) {
    data class VideoRec(
            var bucket: String? = null,
            var contentDisposition: String? = null,
            var contentType: String? = null,
            var crc32c: String? = null,
            var downloadTokens: String? = null,
            var etag: String? = null,
            var generation: String? = null,
            var md5Hash: String? = null,
            var metaGeneration: Int? = null,
            var name: String? = null,
            var size: String? = null,
            var storageClass: String? = null,
            var timeCreated: String? = null,
            var updated: String? = null
    )
}