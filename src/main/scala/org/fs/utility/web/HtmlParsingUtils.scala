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
      ns filter (_.classes contains c)

    def findByClass(c: String): Option[Node] =
      filterByClass(c).headOption

    def cleanText: Seq[String] = ns map (_.cleanText)
  }

  implicit class HtmlNode(n: Node) {
    def cleanText: String = trim(n.text)

    /** Finds all HTML classes defined for the node */
    def classes: Seq[String] =
      n.attribute("class") match {
        case Some(Text(t)) => t split "[\\s]+"
        case _             => Seq.empty
      }
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
