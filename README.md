# circe-golden

[![Build status](https://img.shields.io/github/workflow/status/circe/circe-golden/Continuous%20Integration.svg)](https://github.com/circe/circe-golden/actions)
[![Coverage status](https://img.shields.io/codecov/c/github/circe/circe-golden/master.svg)](https://codecov.io/github/circe/circe-golden)
[![Gitter](https://img.shields.io/badge/gitter-join%20chat-green.svg)](https://gitter.im/circe/circe)
[![Maven Central](https://img.shields.io/maven-central/v/io.circe/circe-golden_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/io.circe/circe-golden_2.12)

Golden testing for [Circe](http://circe.io) encoders and decoders.

## Motivation

One common criticism of [deriving type class instances][derivation] in the context of serialization is that it
makes it too easy to accidentally break compatibility with other systems, since the magic of
derivation can obscure the fact that changes to our data type definitions may also change their
encoding.

For example, suppose we're working with some JSON like this:

```json
{ "id": 12345, "page": "/index.html", "ts": "2019-10-22T14:54:13Z" }
```
And we're decoding it into a Scala case class using Circe:

```scala
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import java.time.Instant

case class Visit(id: Long, page: String, ts: Instant)

object Visit {
  implicit val codecForVisit: Codec[Visit] = deriveCodec
}
```

And because we're responsible people, we're even checking the codec laws:

```scala
import cats.kernel.Eq
import io.circe.testing.{ArbitraryInstances, CodecTests}
import org.scalacheck.Arbitrary
import org.scalatest.flatspec.AnyFlatSpec
import org.typelevel.discipline.scalatest.Discipline
import java.time.Instant

trait VisitTestInstances extends ArbitraryInstances {
  implicit val eqVisit: Eq[Visit] = Eq.fromUniversalEquals
  implicit val arbitraryVisit: Arbitrary[Visit] = Arbitrary(
    for {
      id   <- Arbitrary.arbitrary[Long]
      page <- Arbitrary.arbitrary[String]
      ts   <- Arbitrary.arbitrary[Long].map(Instant.ofEpochMilli)
    } yield Visit(id, page, ts)
  )
}

class VisitSuite extends AnyFlatSpec with Discipline with VisitTestInstances {
  checkAll("Codec[Visit]", CodecTests[Visit].codec)
}
```

This will verify that our JSON codec round-trips values, has consistent error-accumulation and
fail-fast modes, etc. Which is great! Except that if we make a small change to our case class…

```scala
import java.time.Instant

case class Visit(id: Long, page: String, date: Instant)
```

…then our tests will continue to pass, but we won't be able to decode any of the JSON we could previously
decode, and any JSON we produce will be broken from the perspective of external systems that haven't
made equivalent changes.

We can fix this by adding some tests for specific examples:

```scala
import java.time.Instant
import io.circe.testing.CodecTests
import org.scalatest.flatspec.AnyFlatSpec
import org.typelevel.discipline.scalatest.FlatSpecDiscipline

class VisitSuite extends AnyFlatSpec with FlatSpecDiscipline with VisitTestInstances {
  checkAll("Codec[Visit]", CodecTests[Visit].codec)

  val good = """{"id":12345,"page":"/index.html","ts":"2019-10-22T14:54:13Z"}"""
  val value = Visit(12345L, "/index.html", Instant.parse("2019-10-22T14:54:13Z"))

  "codecForVisit" should "decode JSON that's known to be good" in {
    assert(io.circe.jawn.decode[Visit](good) === Right(value))
  }

  it should "produce the expected results" in {
    import io.circe.syntax._
    assert(value.asJson.noSpaces === good)
  }
}
```

The only problem is that it's really unpleasant to do this by hand! Also the "problem" we were
originally trying to solve is only a problem if it happens accidentally. Often we're changing our
data type definition specifically _because_ some schema changed. In that case the fact that we only
have to change our code in one place is actually one of the advantages of type class derivation:
there are fewer things to worry about keeping in sync as our data types and schemas evolve. These
example-based tests make this process a little safer, but at the cost of adding back a lot of the
friction we were using derivation to avoid.

## Golden testing

This library is an attempt to provide the benefits of example-based tests without all the annoying
noise and maintenance. The usage looks like this:

```scala
import io.circe.testing.golden.GoldenCodecTests
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FlatSpecDiscipline

class VisitSuite extends AnyFlatSpec with FlatSpecDiscipline with VisitTestInstances with Configuration {
  checkAll("GoldenCodec[Visit]", GoldenCodecTests[Visit].goldenCodec)
}
```

This is almost identical to our first `VisitSuite`, but the first time we run the test, it will use
the `Arbitrary[Visit]` instance (which we need for the round-trip testing, anyway) to generate an
example `Visit` value, which it will serialize to a JSON string and write to a test resource file.
The next time we run the test, it will find that file and will confirm that the current decoder can
decode it, as well as that the current encoder will produce the same result.

This approach is called ["golden testing"][golden-testing], and this library is inspired specifically
by [hspec-golden-aeson][golden-aeson].

## Usage

Add the dependency to your sbt build:

```scala
libraryDependencies += "io.circe" %% "circe-golden" % "0.1.0" % Test
```

Change all of your `CodecTests` laws-checking tests to `GoldenCodecTests` with `goldenCodec`,
then run your tests as usual. This will check all of the laws you were previously running, plus the
new golden tests.

In general you'll want to check the generated golden test files into version control, since
otherwise you won't get any of the benefits of golden testing in CI (or any other time you test a
fresh check-out).

If you make a change to your codecs that intentionally breaks serialization compatibility, you have
to delete the JSON files in your test resources. You can find these directories by running
`show test:resourceDirectory` in sbt.

## Warnings and known issues

While it's possible to use `GoldenCodecLaws` directly, it's inconvenient, and there's a lot of magic
involved in the `ResourceFileGoldenCodecLaws` implementation. In particular the heuristics for
determining where to write resource files is likely to be kind of fragile. It probably doesn't work
on Windows or many moderately complex cross-builds, for example.

The off-the-shelf golden tests will currently fail if you change your `Arbitrary` instances in such
a way that different seeds produce different values. This generally shouldn't be a problem, since
it's generally likely to be a good idea to isolate changes to your `Arbitrary` instances from
unrelated changes that may break serialization compatibility, anyway. You just have to rebuild your
golden files after changing your `Arbitrary` instances (see the previous section for details).

The golden tests will also fail if you change the number of golden examples to generate. This may
change in the future. In the meantime you have to rebuild your golden files after changing this
configuration.

One extremely inconvenient thing about this library as it exists right now is that every time you
rebuild your golden files, you'll get new ScalaCheck seeds, and therefore new file names, which
means some unnecessary churn in your version control system, as well as less useful diffs. This is
something I'm hoping to address soon.

## Other future work

I'm also planning to add some tools for making it easier to rebuild your golden files, so that this
can be done with a `runMain` from inside the sbt console instead of by manually tracking down and
deleting the resources.

It would probably be possible to make this work for Scala.js projects with some macro magic. I
don't personally care enough, but would be happy to review PRs.

It's possible this functionality will be moved into circe-testing someday, but I kind of doubt it.

## Contributors and participation

All Circe projects support the [Scala code of conduct][code-of-conduct] and we want
all of their channels (Gitter, GitHub, etc.) to be welcoming environments for everyone.

Please see the [Circe contributors' guide][contributing] for details on how to submit a pull
request.

## License

circe-golden is licensed under the **[Apache License, Version 2.0][apache]**
(the "License"); you may not use this software except in compliance with the
License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[golden-aeson]: http://hackage.haskell.org/package/hspec-golden-aeson-0.7.0.0/docs/Test-Aeson-GenericSpecs.html
[apache]: http://www.apache.org/licenses/LICENSE-2.0
[circe]: https://github.com/circe/circe
[code-of-conduct]: https://www.scala-lang.org/conduct/
[contributing]: https://circe.github.io/circe/contributing.html
[derivation]: https://meta.plasm.us/posts/2015/11/08/type-classes-and-generic-derivation/
[golden-testing]: https://ro-che.info/articles/2017-12-04-golden-tests
[sbt-crossproject-74]: https://github.com/portable-scala/sbt-crossproject/issues/74
