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
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.binary.Base64

/**
 * @author FS
 */
trait ApacheHttpHelpers {

  def GET(uri: String) = RequestBuilder.get(uri)
  def POST(uri: String) = RequestBuilder.post(uri)
  def PUT(uri: String) = RequestBuilder.put(uri)
  def DELETE(uri: String) = RequestBuilder.delete(uri)

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

  case class SimpleHttpResponse(code: Int, headers: Seq[(String, String)], body: Array[Byte]) {
    lazy val bodyString: String = bodyString("UTF-8")
    def bodyString(charset: String): String = new String(body, charset)
  }

  //
  // Implicits
  //

  implicit def buildRequest(rb: RequestBuilder): HttpUriRequest =
    rb.build()

  implicit class RichRequestBuilder(rb: RequestBuilder) {
    def params(params: Map[String, String]): RequestBuilder = {
      params.foreach {
        case (n, v) => rb.addParameter(n, v)
      }
      rb
    }

    def basicAuth(username: String, password: String): RequestBuilder = {
      val encoded = Base64.encodeBase64String(s"$username:$password".getBytes("UTF-8"))
      rb.setHeader("Authorization", "Basic " + encoded)
    }
  }

  implicit class RichHttpClient(client: HttpClient) {
    def request(request: HttpUriRequest): SimpleHttpResponse = {
      val resp = client.execute(request)
      val entity = resp.getEntity
      try {
        SimpleHttpResponse(
          code = resp.getStatusLine.getStatusCode,
          headers = resp.getAllHeaders map (h => (h.getName -> h.getValue)),
          body = IOUtils.toByteArray(entity.getContent)
        )
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
