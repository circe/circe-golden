package io.circe.examples

import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.github.writethemfirst.Approbation
import io.circe._
import io.circe.parser._
import io.circe.syntax._

import scala.util._

class ApprobationSuite extends FixtureAnyFlatSpec with Matchers with Approbation {
  it should "serialize MyList[Foo]" in { approver =>
    val list = MyList[Foo](List(Bar(1, "aa"), Baz(List("x", "y"))))
    approver.verify(list.asJson)
    val data = approver.approvedAndReceivedPaths.approvedContent
    val myList = decode[MyList[Foo]](data).right.get
    myList shouldBe list
  }

}
