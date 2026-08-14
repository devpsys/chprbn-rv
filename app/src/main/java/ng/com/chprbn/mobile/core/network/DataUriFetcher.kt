package ng.com.chprbn.mobile.core.network

import android.net.Uri
import android.util.Base64
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer

/**
 * Coil 2.x ships fetchers for http(s)/file/content/android.resource/asset URIs
 * but none for `data:` URIs. Every photo the API sends is a raw Base64 blob
 * normalized into a `data:image/...;base64,...` string by
 * [normalizeApiPhotoToDataUri], so without this registered on the app's
 * [ImageLoader], AsyncImage silently falls back to its placeholder for every
 * candidate/user photo in the app — no crash, no log, just a blank avatar.
 */
class DataUriFetcher(private val uri: Uri, private val options: Options) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val schemeSpecificPart = uri.schemeSpecificPart.orEmpty()
        val mimeType = schemeSpecificPart.substringBefore(";base64,", missingDelimiterValue = "image/jpeg")
        val base64Payload = schemeSpecificPart.substringAfter(',', missingDelimiterValue = "")
        val bytes = Base64.decode(base64Payload, Base64.DEFAULT)

        return SourceResult(
            source = ImageSource(Buffer().write(bytes), options.context),
            mimeType = mimeType,
            dataSource = DataSource.MEMORY,
        )
    }

    class Factory : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.scheme != "data") return null
            return DataUriFetcher(data, options)
        }
    }
}
