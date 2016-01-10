package org.fs.utility.web

import java.security.SecureRandom
import java.security.cert.X509Certificate

import scala.collection.JavaConversions._
import scala.io.Source

import org.apache.http.client.CookieStore
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.cookie.Cookie
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

import javax.net.ssl._

/**
 * @author FS
 */
trait ApacheHttpHelpers {

  val trustAllSslContext: SSLContext = {
    val trustAllCerts = Array[TrustManager](
      new X509TrustManager() {
        override def getAcceptedIssuers = Array.empty
        override def checkClientTrusted(certs: Array[X509Certificate], authType: String) = {}
        override def checkServerTrusted(certs: Array[X509Certificate], authType: String) = {}
      }
    )
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, new SecureRandom)
    sslContext
  }

  def makeSimpleClientWithStore(): (HttpClient, CookieStore) = {
    val cookieStore = new BasicCookieStore()
    val httpClient = {
      val clientBuilder = HttpClients.custom()
      clientBuilder.setDefaultCookieStore(cookieStore)
      clientBuilder.setSSLContext(trustAllSslContext)
      clientBuilder.build()
    }
    (httpClient, cookieStore)
  }

  //
  // Implicit classes
  //

  implicit class RichRequestBuilder(rb: RequestBuilder) {
    def params(params: Map[String, String]): RequestBuilder = {
      params.foreach {
        case (n, v) => rb.addParameter(n, v)
      }
      rb
    }
  }

  implicit class RichHttpClient(client: HttpClient) {
    def executeReturningString(request: HttpUriRequest): String = {
      val resp = client.execute(request)
      val entity = resp.getEntity
      try {
        Source.fromInputStream(entity.getContent).mkString
      } finally {
        EntityUtils.consume(entity)
      }
    }
  }

  implicit class RichCookieStore(cs: CookieStore) {
    def cookies: Seq[Cookie] = cs.getCookies
  }
}

object ApacheHttpHelpers extends ApacheHttpHelpers
