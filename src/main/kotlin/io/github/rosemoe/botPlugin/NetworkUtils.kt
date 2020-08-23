package io.github.rosemoe.botPlugin

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

private var sInstalled = false

@Synchronized
@Throws(Exception::class)
fun installIfNot() {
    if (sInstalled) {
        return
    }
    sInstalled = true
    trustAllHttpsCertificates()
    ignoreSsl()
}

@Throws(Exception::class)
fun trustAllHttpsCertificates() {
    val trustAllCerts = arrayOfNulls<TrustManager>(1)
    val tm: TrustManager = MyTrustManager()
    trustAllCerts[0] = tm
    val sc = SSLContext.getInstance("SSL")
    sc.init(null, trustAllCerts, null)
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
}

internal class MyTrustManager : TrustManager, X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate?> {
        return Array(0) { null }
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {
    }
}

@Throws(Exception::class)
fun ignoreSsl() {
    val hv = HostnameVerifier { _, _ -> true }
    trustAllHttpsCertificates()
    HttpsURLConnection.setDefaultHostnameVerifier(hv)
}
