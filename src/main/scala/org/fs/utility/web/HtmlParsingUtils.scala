package org.fs.utility.web

import scala.xml._

import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

trait HtmlParsingUtils {
  private val saxFactory = new SAXFactoryImpl()

  def parseElement(bodyString: String): Elem = {
    val xmlParser = XML.withSAXParser(saxFactory.newSAXParser())
    xmlParser.loadString(bodyString)
  }

  implicit class HtmlNodeSeq(ns: NodeSeq) {
    def filterByClass(c: String): NodeSeq =
      ns filter (_.attribute("class") match {
        case Some(Text(t)) => t split " " contains c
        case _             => false
      })
  }

  implicit class HtmlNode(n: Node) {
    def cleanText: String = trim(n.text)
  }

  /** More appropriate version of trim than commons-lang3's StringUtils.strip() */
  private def trim(s: String): String = {
    def isNotSpace(c: Char) = !Character.isWhitespace(c) && !Character.isSpaceChar(c)
    val startIdx = s.indexWhere(isNotSpace)
    if (startIdx == -1)
      ""
    else {
      val endIdx = s.lastIndexWhere(isNotSpace)
      s.substring(startIdx, endIdx + 1)
    }
  }
}

object HtmlParsingUtils extends HtmlParsingUtils
