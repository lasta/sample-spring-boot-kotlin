# Spring Boot 2 に Kotlin で入門してみた - 2. Hello World 編
[1. 環境構築編](https://qiita.com/lasta/items/4279a01f205f9cc5e0d4) の続きになります。

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

## 2. Hello World 編
### エントリポイント
Hello World を返却するAPIの前に、エントリポイントの解説のみします。
エントリポイントは `src/main/kotlin/com/lasta/api/sample/SampleApplication.kt` の `main` 関数になります。
この関数はプロジェクト作成時に自動作成され、下記のような実装になっています。

```kotlin:SampleApplication.kt
package com.lasta.api.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SampleApplication

fun main(args: Array<String>) {
    runApplication<SampleApplication>(*args)
}
```

この実装に関する解説は [こちら](https://spring.io/guides/gs/spring-boot/) にあります。(Java)
ひとつひとつ訳します。
[こちらの記事](https://spring-boot-camp.readthedocs.io/ja/latest/01-HelloWorld.html) も参考にしました。

* `@SpringBootApplication`
    * 下記3つのアノテーションをあわせたもの
    * `@Configuration`
        * この関数配下のパッケージの bean クラス (`@Bean` アノテーションつきのメソッド) をアプリケーションのコンテキストに追加する
            * DI コンテナに Bean を登録する
    * `@EnableAutoConfiguration`
        * Spring Boot 起動時に自動設定群を有効化する
    * `@ComponentScan`
        * Spring がこの関数配下のパッケージの Component, Configuration, Service を読み込み、 Controller がアクセスできるようにする
        * 探せるアノテーションは下記
            * `@org.springframework.stereotype.Component`
            * `@org.springframework.stereotype.Controller`
            * `@org.springframework.stereotype.Service`
            * `@org.springframework.stereotype.Repository`
            * `@org.springframework.stereotype.Configuration`
            * `@org.springframework.stereotype.RestController`
            * `@org.springframework.stereotype.ControllerAdvice`
            * `@javax.annotation.ManagedBean`
            * `@javax.inject.Named`
* `SpringBootApplication#runApplication`
    * Spring Boot アプリケーションを起動する

ここまでで DI や Bean を始めとした用語が出てきていますが、これについては今後の記事で解説します。
「Spring Boot DI」等で Google や Qiita で検索するとたくさん出てくるので、そちらを参考にしたほうが正確かもしれません。

### Spring MVC
Spring MVC は MVC パターンを採用しています。
下記のような流れでリクエストが処理され返却されます。

```
            1.Request
+--------+ -----------> +------------+  2.Update state   +-------+
| client |              | Controller | ----------------> | Model |
+--------+ <----------- +------------+                   +-------+
            4.Response        | 3. Generate Response Data    ^
                              v                              .
                        +------------+                       .
                        |    View    | .......................
                        +------------+  (Refer data)
```

このため、エンドポイントの定義は Controller で行います。

### Hello World を返却してみる
いよいよ実装に入ります。
`SampleApplication.kt` が `com.lasta.api.sample` 配下にあるため、今後作成するクラスおよびパッケージは、これより下に配置します。

上述の通りエンドポイントは Controller に定義するので、 `controller` パッケージを作成の上実装します。

```kotlin:HelloController.kt
package com.lasta.api.sample.controller

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["hello"])
class HelloController {

    @GetMapping(path = ["world"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun helloWorld(): String {
        return "hello world"
    }
}
```

* `@RestController`
    * Spring MVC がリクエストを扱うための準備
    * `@Controller` と `@ResponseBody` の2つを内包している
* `@RequestMapping`
    * リクエストを定義
    * `path`
        * リクエストパスを定義
        * `HelloController` クラス配下はすべて `/hello` 配下のリクエストを扱う
* `@GetMapping`
    * GET リクエストを定義
    * `path`
        * リクエストパスをい定義
        * `/hello/world` のパスで GET リクエストされた場合、 `helloWord` 関数が実行される
    * `produces`
        * 返却する Media-Type を定義する
    * `consumes`
        * Request Body として受け付ける Content-Type を定義する
    * `headers`
        * 受け付けるヘッダ情報を定義する

ここまでで `/hello/world` にアクセスが来たら `hello world` を返却することができます。
起動をして動作を確認します。

* 下記のいずれかの方法で起動
    * IntelliJ IDEA で `sample [bootRun]` を Run または Debug
    * IntelliJ IDEA で Gradle -> Tasks -> application -> bootRun
    * `./gradlew bootRun`
* コンソールに `Started SampleApplicationKt` が出力されることを確認
* ブラウザまたは `curl` で `http://localhost:8080/hello/world` にアクセス
    * `hello world` が返却される :tada:
    * ![browser]( ./assets/2/browser.png )

## 次回
* GET リクエストのパラメータ解析
