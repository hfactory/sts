package net.distfp.sts

import java.io.{FileReader, BufferedReader, File}

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

import StsControl._

object StsIO {
  val EOL = "\n"

  /** Returns the lines of a file. */
  def readLines(fileName: String): Try[Seq[String]] =
    Try { new BufferedReader(new FileReader(fileName)) } flatMap { r =>
      readLines(r) thenAlways { r.close() }
    } recoverWith {
      case NonFatal(e) => Failure(new StsException(s"Failed reading file '$fileName'", e))
    }

  def readLines(r: BufferedReader): Try[Seq[String]] = Try {
    Stream.continually(r.readLine()).takeWhile(_ != null).toList
  } recoverWith {
    case NonFatal(e) => Failure(new StsException("Failed reading data"))
  }

  /** Returns the contents of a file as a string.
    *
    * If keepLines is true, the newlines are included in the returned string.
    * Otherwise, they're not.
    */
  def readFileContents(fileName: String, keepLines: Boolean = true): Try[String] =
    Try { new BufferedReader(new FileReader(fileName)) } flatMap { r =>
      readContents(r, keepLines) thenAlways { r.close() }
    } recoverWith {
      case NonFatal(e) => Failure(new StsException(s"Failed reading file '$fileName'", e))
    }

  def readContents(r: BufferedReader, keepLines: Boolean = true): Try[String] =
    readLines(r) map (_.mkString(if (keepLines) "\n" else ""))

  /** Returns the lines between the specified delimiters. */
  private[sts] def extractLines(lines: Seq[String], start: String, stop: String): Seq[String] =
    lines.dropWhile(_ != start).drop(1).takeWhile(_ != stop)

  private[sts] def checkPresent(fileName: String): Try[Unit] =
    if (new File(fileName).exists()) Success(()) else Failure(new StsException(s"File '$fileName' not found"))

  private[sts] def checkAbsent(fileName: String): Try[Unit] =
    if (new File(fileName).exists()) Failure(new StsException(s"File '$fileName' already exists")) else Success(())
}

