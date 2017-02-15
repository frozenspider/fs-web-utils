package org.fs.utility.web.http

import java.nio.charset.Charset
import org.apache.http.entity.ContentType

/**
 * @author FS
 */
case class SimpleHttpResponse(code: Int, headers: Seq[(String, String)], body: Array[Byte]) {
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
  def findHeader(headerName: String): Option[String] =
    headers find (_._1 == headerName) map (_._2)

  /** @return Content-Type header, if known. */
  lazy val contentTypeOption: Option[String] =
    findHeader("Content-Type")

  /** @return Content-Encoding header, if known. */
  lazy val contentEncodingOption: Option[String] =
    findHeader("Content-Encoding")
}
