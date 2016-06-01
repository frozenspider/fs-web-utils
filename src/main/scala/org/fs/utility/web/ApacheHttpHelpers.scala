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
import scala.collection.GenTraversableOnce

/**
 * @author FS
 */
trait ApacheHttpHelpers {

  def GET(uri: String) = RequestBuilder.get(uri)
  def POST(uri: String) = RequestBuilder.post(uri)
  def PUT(uri: String) = RequestBuilder.put(uri)
  def DELETE(uri: String) = RequestBuilder.delete(uri)

  /** SSL context which completely disables certificate detailed checks */
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

  def simpleClientWithStore(sslContextOption: Option[SSLContext] = None): (HttpClient, CookieStore) = {
    val cookieStore = new BasicCookieStore()
    val httpClient = {
      val clientBuilder = HttpClients.custom()
      clientBuilder.setDefaultCookieStore(cookieStore)
      sslContextOption map clientBuilder.setSSLContext
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

  /** Enables using RequestBuilder in place of HttpUriRequest */
  implicit def buildRequest(rb: RequestBuilder): HttpUriRequest =
    rb.build()

  /** While this enhances the RequestBuilder with some shortcuts, its nature remains mutable */
  implicit class RichRequestBuilder(rb: RequestBuilder) {
    def param(name: String, value: String): RequestBuilder =
      rb.addParameter(name, value)

    def params(params: Map[String, String]): RequestBuilder =
      this.params(params.toSeq)

    def params(params: Seq[(String, String)]): RequestBuilder =
      params.foldLeft(rb) {
        case (rb, (n, v)) => rb.param(n, v)
      }

    def header(name: String, value: String): RequestBuilder =
      rb.addHeader(name, value)

    def headers(headers: Map[String, String]): RequestBuilder =
      this.headers(headers.toSeq)

    def headers(headers: Seq[(String, String)]): RequestBuilder =
      headers.foldLeft(rb) {
        case (rb, (n, v)) => rb.header(n, v)
      }

    def basicAuth(username: String, password: String): RequestBuilder = {
      val encoded = Base64.encodeBase64String(s"$username:$password".getBytes("UTF-8"))
      rb.header("Authorization", s"Basic $encoded")
    }
  }

  implicit class RichHttpClient(client: HttpClient) {
    /** Make a request, read the response completely and do the cleanup */
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
