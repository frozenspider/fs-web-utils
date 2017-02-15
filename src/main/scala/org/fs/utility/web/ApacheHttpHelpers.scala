package org.fs.utility.web

import java.nio.charset.Charset
import java.security.SecureRandom
import java.security.cert.X509Certificate

import scala.collection.JavaConversions._

import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.cookie.Cookie
import org.apache.http.entity.ContentType
import org.apache.http.impl.client._
import org.apache.http.util.EntityUtils

import javax.net.ssl._

/**
 * Helpers for working with Apache HttpClient
 *
 * @author FS
 */
trait ApacheHttpHelpers {

  def GET(uri: String) = RequestBuilder.get(uri)
  def HEAD(uri: String) = RequestBuilder.head(uri)
  def POST(uri: String) = RequestBuilder.post(uri)
  def PUT(uri: String) = RequestBuilder.put(uri)
  def PATCH(uri: String) = RequestBuilder.patch(uri)
  def DELETE(uri: String) = RequestBuilder.delete(uri)

  /** SSL context which completely disables certificate detailed checks */
  def trustAllSslContext: SSLContext = ApacheHttpHelpers.trustAllSslContext

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
    import ApacheHttpHelpers.Headers._

    /** @return content charset, if specified */
    lazy val charsetOption: Option[Charset] =
      contentTypeOption map ContentType.parse map (_.getCharset)

    /** @return content charset, if specified, or default ISO-8859-1 as per HTTP/1.1 standard */
    lazy val charset: Charset =
      charsetOption getOrElse ContentType.DEFAULT_TEXT.getCharset

    /** @return body as a string using the content charset if any, or ISO-8859-1 as per HTTP/1.1 */
    lazy val bodyString: String =
      new String(body, charset)

    /** @return body as a UTF-8 string */
    lazy val bodyStringUTF8: String =
      new String(body, "UTF-8")

    /**
     * Obtain the value of a header with a given name, if known.
     * If several same-named headers are present, either may be returned.
     */
    def findHeader(headerName: String): Option[String] = headers find (_._1 == headerName) map (_._2)

    /** @return Content-Type header, if known. */
    lazy val contentTypeOption: Option[String] = findHeader(`Content-Type`)

    /** @return Content-Encoding header, if known. */
    lazy val contentEncodingOption: Option[String] = findHeader(`Content-Encoding`)
  }

  //
  // Implicits
  //

  /** Enables using RequestBuilder in place of HttpUriRequest */
  implicit def buildRequest(rb: RequestBuilder): HttpUriRequest =
    rb.build()

  /** While this enhances the RequestBuilder with some shortcuts, its nature remains mutable */
  implicit class RichRequestBuilder(rb: RequestBuilder) {
    def addParameters(params: Map[String, String]): RequestBuilder =
      this.addParameters(params.toSeq)

    def addParameters(params: Seq[(String, String)]): RequestBuilder =
      params.foldLeft(rb) {
        case (rb, (n, v)) => rb.addParameter(n, v)
      }

    def addHeaders(headers: Map[String, String]): RequestBuilder =
      this.addHeaders(headers.toSeq)

    def addHeaders(headers: Seq[(String, String)]): RequestBuilder =
      headers.foldLeft(rb) {
        case (rb, (n, v)) => rb.addHeader(n, v)
      }

    def addBasicAuth(username: String, password: String): RequestBuilder = {
      val encoded = Base64.encodeBase64String(s"$username:$password".getBytes("UTF-8"))
      rb.addHeader("Authorization", s"Basic $encoded")
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

object ApacheHttpHelpers extends ApacheHttpHelpers {
  object Headers {
    val `Content-Type` = "Content-Type"
    val `Content-Encoding` = "Content-Encoding"
  }

  /** SSL context which completely disables certificate detailed checks */
  override val trustAllSslContext: SSLContext = {
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
}
