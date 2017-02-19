package org.fs.utility.web

import org.fs.utility.web.http.ApacheHttpHelpers

/**
 * Imports aggregator trait for fs-web-utils
 *
 * @author FS
 */
trait Imports
  extends ApacheHttpHelpers
  with HtmlParsingUtils

/**
 * Imports aggregator object for fs-web-utils
 *
 * @author FS
 */
object Imports extends Imports
