package io.circe.examples

import org.scalatest.flatspec. FixtureAnyFlatSpec
import com.github.writethemfirst.Approbation
import io.circe.syntax._


class ApprobationSuite extends FixtureAnyFlatSpec with Approbation {
  it should "serialize MyList[Foo]" in { approver =>
    val data=MyList[Foo](List(Bar(1, "aa"),Baz(List("x","y"))))
    approver.verify(data.asJson)
  }
}
