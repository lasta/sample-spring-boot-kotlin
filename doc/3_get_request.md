# Spring Boot 2 に Kotlin で入門してみた - 3. GET リクエストのパラメータ解析 編
[2. Hello World 編](https://qiita.com/lasta/items/aaf87d6ca811ae2170a4) の続きになります。

新規でAPIサーバを作りたくなり、Spring Boot 2 を用いて作成してみます。
Kotlin Fest 2018で、Kotlin でも [Kotlin ネイティブなフレームワーク(ktorなど)][ktor] ではなくて [Java ネイティブなフレームワーク][Kotlin Fest 2018] を使うのがおすすめという話があったので、 Spring Boot 2 を選びました。

入門時の私のスキルは下記の通りです。

* Java : 2年半 (サーバサイド、コンバータ)
* Kotlin : 1年 (コンバータ)
* サーバサイドJava : 2年半 (独自フレームワーク)
* IntelliJ IDEA : 1年半
* Spring Boot : 未経験

上述の通り、 Spring Boot は未経験のため、誤りがある可能性があります。
誤りがあった際には、ご指摘いただけると嬉しいです!
一方で完全に未経験のため、同様にこれから始める方の参考になると思います。

目標は、 Solr を用いて検索した結果をもとに MySQL からデータを取得して返却する API を作成することとしました。

[ソースコードは Github に置きました][github]

[ktor]: https://qiita.com/lasta/items/2c25ae5a875ba8da4f8a
[Kotlin Fest 2018]: http://tech.connehito.com/entry/2018/08/31/131552
[github]: https://github.com/lasta/sample-spring-boot-kotlin/tree/qiita/2-helloworld

## 参考書
* [Spring徹底入門 Spring FrameworkによるJavaアプリケーション開発](https://www.amazon.co.jp/gp/product/B01IEWNLBU/ref=oh_aui_d_detailpage_o01_?ie=UTF8&psc=1)

## 実装の前に
下記の2つについて予め理解しておく必要があるため、その解説を先に行います。

* DI (Dependency Injection)
* 3層アーキテクチャ

### DI - Dependency Injection
DI はよく「依存性の注入」と訳されますが、「注入」という言葉が微妙にずれているため、あまり日本語では考えないほうが良いです。(個人的な意見です)
あえて日本語で解説するならば、「依存している外部のなにか (インスタンス、定数、変数) を外から入れてあげること」です。
詳しい解説は [猿でも分かる! Dependency Injection: 依存性の注入](https://qiita.com/hshimo/items/1136087e1c6e5c5b0d9f) がわかりやすかったです。

Spring Boot では、Webアプリケーションでよくある3層アーキテクチャ (後述) を採用しており、その各層 + α 間はできるだけ疎結合にするべきです。
そうすることにより、実装に柔軟性ができ、また単体テストが非常にやりやすくなります。

Spring Boot では、下記のアノテーションを付与したクラスがDIコンテナに登録されます。

* `@org.springframework.stereotype.Component`
* `@org.springframework.stereotype.Controller`
* `@org.springframework.stereotype.Service`
* `@org.springframework.stereotype.Repository`
* `@org.springframework.stereotype.Configuration`
* `@org.springframework.stereotype.RestController`
* `@org.springframework.stereotype.ControllerAdvice`
* `@javax.annotation.ManagedBean`
* `@javax.inject.Named`

### 3層アーキテクチャ
前回で、Spring Framework の Spring MVC の解説を簡単に行いました。
今回は API サーバであり、Viewの生成は不要のため、少し流れを変えます。

```
                            +------------+
                            |    Form    |
                            +------------+
                            ^  .
  1.1. Parameter Validation .  . 1.2. Validated Parameter
                1.Request   .  v
    +--------+ -----------> +------------+  2.Request Param > +----------+
    | client |              | Controller | <----------------> | Service  |
    +--------+ <----------- +------------+ <5.Response Object +----------+
                4.Response                                      |     ^
                                                3. Request data |     | 4. Requested Data
                                                                v     |
                                                              +----------+
                                                              |   DAO    |
                                                              +----------+
                                                              Access to External Resource (DB, Solr, ElasticSearch, ...)
                                                              to get Data.
```

それぞれの要素は、下記のように対応します。

* Client
  * ブラウザや `curl` など
* Controller 層
  * リクエストを受け取り、値を返却するエンドポイントとなるクラス
  * Spring Framework では一般に `XxxController` というクラス名が用いられる
  * 基本的にロジックは持たず、処理は Model 層に任せる
  * リクエストパラメータのみを Form 層に分けることがある
* Service 層
  * ビジネスロジックを持つ
  * `XxxService` というインタフェース名と、 `XxxServiceImpl` というクラス名が用いられる
* DAO 層
  * 外部リソース(DB 等)へアクセスするインタフェース及び実装
  * `XxxRepository` というインタフェース名と `XxxRepositoryImpl` というクラス名が用いられる
  * 今回は外部リソースへのアクセスはしないため登場しません

## 3. GET リクエストのパラメータ解析 編
今回作成するAPIは下記のとおりです。

* path : `/greeting`
* parameter
  * `phase` (必須)
    * 朝 / 昼 / 夜を指定
    * Enum型
      * `morning`
      * `noon`
      * `evening`
    * デフォルト値 なし
  * `name`
    * 名前を指定
    * String 型
    * デフォルト値 なし

### 実装と解説
パッケージ構成は下記の通りです。

```
src/main/kotlin/com/lasta/api/sample
├── SampleApplication.kt
├── constant
│   └── GreetingPhase.kt
├── controller
│   └── GreetingController.kt
├── model
│   └── converter
│       └── GreetingPhaseConverter.kt
└── service
    ├── GreetingService.kt
    └── impl
        └── GreetingServiceImpl.kt
```

#### Controller

```kotlin:GreetingController.kt
package com.lasta.api.sample.controller

import com.lasta.api.sample.constant.GreetingPhase
import com.lasta.api.sample.model.converter.GreetingPhaseConverter
import com.lasta.api.sample.service.GreetingService
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.NotNull

@Validated
@RestController
@RequestMapping(path = ["greeting"])
class GreetingController(private val service: GreetingService) {

    @GetMapping(produces = [MediaType.TEXT_PLAIN_VALUE])
    fun greet(@NotNull @RequestParam(value = "phase") phase: GreetingPhase,
              @RequestParam(value = "name") name: String?): String {
        return service.greet(phase, name)
    }

    @InitBinder
    fun initBinder(webDataBinder: WebDataBinder) {
        webDataBinder.registerCustomEditor(GreetingPhase::class.java, GreetingPhaseConverter())
    }
}
```

##### Controller クラスのアノテーション
* `@Validated`
  * `@Valid` が付与されたリクエストパラメータが全て Valid かどうか検査する
* `@RestController`
  * Spring MVC がリクエストを扱うための準備
* `@RequestMapping`
  * リクエストを定義
  * path
    * リクエストパスを定義

##### Controller クラスのコンストラクタ
* `@Service` アノテーションが付与されている `GreetingServiceImpl` (後述) はDIコンテナに登録されているので、Inject できる
* 下記のように記載することも可能

```kotlin:一部のみ抜粋
@RestController
class GreetingControlle {
    @Inject
    lateinit var service: GreetingService
}
```

##### Controller#greet メソッドの実装
* `@GetMapping`
  * GET リクエストを定義
  * `produces`
    * 返却する Media-Type を定義する
* `@NotNull @RequestParam(value = "phase") phase: GreetingPhase)`
  * `@RequestParam`
    * リクエストパラメータを定義
    * `value`
      * リクエストパラメータのキーを指定
  * `@NotNull`
    * リクエストパラメータが Not null でなければならないというバリデーションを行う
  * 変数 `phase` は 列挙型 `GreetingPhase` 
* `@RequestParam(value = "name") name: String?`
  * nullable な String 型のリクエストキー `name` を定義

##### controller#initBinder メソッドの実装
* `@InitBinder`
  * 型変換の方法を登録
    * ここでは、 `String` から `GreetingPhase` に変換する (実装は後述)
* `webDataBinder.registerCustomEditor(GreetingPhase::class.java, GreetingPhaseConverter())`
  * システム全体の型変換の方法をもつオブジェクト `webDataBinder` に対し、独自に定義した型 (`GreetingPhase`) とその変換方法 `GreetingPhaseConverter` を登録

#### Service

```kotlin:GreetingService.kt
package com.lasta.api.sample.service

import com.lasta.api.sample.constant.GreetingPhase

interface GreetingService {
    fun greet(phase: GreetingPhase, name: String?): String
}
```

```kotlin:GreetingServiceImpl.kt
package com.lasta.api.sample.service.impl

import com.lasta.api.sample.constant.GreetingPhase
import com.lasta.api.sample.service.GreetingService
import org.springframework.stereotype.Service

@Service
class GreetingServiceImpl: GreetingService {
    override fun greet(phase: GreetingPhase, name: String?): String {
        if (name.isNullOrBlank()) {
            return "${phase.greeting}."
        }
        return "${phase.greeting}, $name."
    }
}
```

##### GreetingService
インタフェースとして定義することにより、境界を明確化することができ、単体テストを行いやすくなります。

##### GreetingServiceImpl
`greet` のロジックを実装したものになります。
`@Service` アノテーションを付与して DI コンテナに登録しています。
それ以外は特に解説は不要ですね。

#### Constant

```kotlin:GreetingPhase.kt
package com.lasta.api.sample.constant

enum class GreetingPhase(val greeting: String) {
    MORNING(greeting = "Good Morning"),
    NOON(greeting = "Good afternoon"),
    EVENING(greeting = "Good evening");

    companion object {
        @Throws(IllegalArgumentException::class)
        fun fromValue(text: String?): GreetingPhase {
            text ?: throw IllegalArgumentException()

            return GreetingPhase.valueOf(text.toUpperCase())
        }
    }
}
```
##### GreetingPhase
列挙型のメンバ自体は特に解説することはありません。
`fromValue` メソッドはリクエストパラメータ `phase` の値を列挙型の要素に変換するためのロジックです。

#### Model

```kotlin:GreetingPhaseConverter.kt
package com.lasta.api.sample.model.converter

import com.lasta.api.sample.constant.GreetingPhase
import java.beans.PropertyEditorSupport

class GreetingPhaseConverter : PropertyEditorSupport() {
    @Throws(IllegalArgumentException::class)
    override fun setAsText(text: String?) {
        value = GreetingPhase.fromValue(text)
    }
}
```

##### GreetingPhaseConverter
`GreetingPhaseController#initBinder` で呼び出していたクラスです。
リクエストパラメータをオブジェクトに変換するロジックを持つ `PropertyEditorSupport` を拡張し、文字列を列挙型 `GreetingPhase` に変換するロジックを `setAsText` メソッドで登録します。

### 実行結果
```bash
#### 成功する
curl -s 'http://localhost:8080/greeting?phase=morning&name=lasta'
curl -s 'http://localhost:8080/greeting?phase=noon&name=lasta'
curl -s 'http://localhost:8080/greeting?phase=evening&name=lasta'
curl -s 'http://localhost:8080/greeting?phase=morning'
curl -s 'http://localhost:8080/greeting?phase=morning&name='

#### バリデーションエラー
curl -s 'http://localhost:8080/greeting?name=lasta'
curl -s 'http://localhost:8080/greeting?phase=&name=lasta'
curl -s 'http://localhost:8080/greeting?phase=hello&name=lasta'
```

```bash:ResponseHeader
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 20
Date: Sun, 23 Sep 2018 04:31:42 GMT

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 22
Date: Sun, 23 Sep 2018 04:31:42 GMT

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 20
Date: Sun, 23 Sep 2018 04:31:42 GMT

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 13
Date: Sun, 23 Sep 2018 04:31:42 GMT

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 13
Date: Sun, 23 Sep 2018 04:31:42 GMT

HTTP/1.1 406 
Transfer-Encoding: chunked
Date: Sun, 23 Sep 2018 04:31:42 GMT

HTTP/1.1 406 
Transfer-Encoding: chunked
Date: Sun, 23 Sep 2018 04:31:42 GMT

HTTP/1.1 406 
Transfer-Encoding: chunked
Date: Sun, 23 Sep 2018 04:31:42 GMT
```

```bash:ResponseBody
Good Morning, lasta.
Good afternoon, lasta.
Good evening, lasta.
Good Morning.
Good Morning.
```

#### 真偽値表
| phase   | name    | 結果                   | HTTP Status code |
|---------|---------|------------------------|------------------|
| morning | lasta   | Good Morning, lasta.   | 200              |
| noon    | lasta   | Good afternoon, lasta. | 200              |
| evening | lasta   | Good evening, lasta.   | 200              |
| morning | (null)  | Good Morning.          | 200              |
| morning | (empty) | Good Morning.          | 200              |
| (null)  | lasta   |                        | 406              |
| (empty) | (null)  |                        | 406              |
| (hello) | (null)  |                        | 406              |

リクエストパラメータが不正の場合は 400 Bad Request の上でその原因を明示して上げたほうが親切ですが、それについては後ほど行います。

### Form の利用
ここまでで、 GET リクエストを受け取る API の実装およびリクエストパラメータのバリデーションを行いました。
今回はパラメータが2つだけなのであまり問題にはなりませんでしたが、もしリクエストパラメータが10個、20個になったらどうなるでしょうか?
`@GetMapping` のメソッドの引数の数が大変なことになりますね。
また、複数のパラメータを複合的に見ないとバリデーションできない場合もありますが、現在の方法では対応できません。

そんなシーンに対応できるのが、 Form クラスです。
リクエストパラメータの値を Form クラスに格納するようにします。
また、バリデーションも Form クラスで行うことで、より高度なバリデーションを行うことができるようになります。

#### Form クラスの実装
```kotlin:GreetingForm.kt
package com.lasta.api.sample.model.form

import com.lasta.api.sample.constant.GreetingPhase
import java.io.Serializable
import javax.validation.constraints.NotNull

class GreetingForm : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    @NotNull
    lateinit var phase: GreetingPhase

    var name: String? = null
}
```

##### GreetingForm
フィールドはリクエストパラメータ名と同様に `phase` と `name` を用意しました。
型は `GreetingPhase` としています。
この型の変換は `GreetingController#initBinder` で行われます。
`phase` は `@NotNull` なので `lateinit` で、 `name` は nullable なので、デフォルト値として `null` を代入しています。

#### Controller クラスの改修
From クラスを作成したので、これを用いるように Controller クラスを改修します。

```diff:GreetingController.kt
  package com.lasta.api.sample.controller
  
  import com.lasta.api.sample.constant.GreetingPhase
  import com.lasta.api.sample.model.converter.GreetingPhaseConverter
  import com.lasta.api.sample.model.form.GreetingForm
  import com.lasta.api.sample.service.GreetingService
  import org.springframework.http.MediaType
  import org.springframework.validation.annotation.Validated
  import org.springframework.web.bind.WebDataBinder
  import org.springframework.web.bind.annotation.GetMapping
  import org.springframework.web.bind.annotation.InitBinder
  import org.springframework.web.bind.annotation.ModelAttribute
  import org.springframework.web.bind.annotation.RequestMapping
  import org.springframework.web.bind.annotation.RestController
- import javax.validation.constraints.NotNull
  
- @Validated
  @RestController
  @RequestMapping(path = ["greeting"])
  class GreetingController(private val service: GreetingService) {
  
-     @GetMapping(produces = [MediaType.TEXT_PLAIN_VALUE])
-     fun greet(@NotNull @RequestParam(value = "phase") phase: GreetingPhase,
-               @RequestParam(value = "name") name: String?): String {
-         return service.greet(phase, name)
-     }
  
+     @GetMapping(produces = [MediaType.TEXT_PLAIN_VALUE])
+     fun greet(@ModelAttribute @Validated form: GreetingForm): String {
+         return service.greet(form.phase, form.name)
+     }
  
      @InitBinder
      fun initBinder(webDataBinder: WebDataBinder) {
          webDataBinder.registerCustomEditor(GreetingPhase::class.java, GreetingPhaseConverter())
      }
  }
```

##### GreetingController#greet
`GreetingForm` に値を渡すために `@ModelAttribute` を付与します。
また、Validationをしてほしいためこの値に `@Validated` を付与します。
これで、改修前と同様の動作をします。

#### Form のプロパティ名について
リクエストパラメータのキーがスネークケースやケバブケースの場合、Formクラスへのマッピングが失敗します。
そのため、Formクラスの各プロパティに明示的にキー名を指定したいのですが、それを行う機能は無いようです。
[独自アノテーションを作成しようとして挫折](http://shibuya-3percent.hatenablog.com/entry/2016/07/03/190549)し、結局 `ModelMapper` を用いたという記事がありました。
なにか良い方法はないものでしょうか?

## 次回
DB 接続 (予定)
