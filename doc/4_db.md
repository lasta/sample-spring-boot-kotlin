# Spring Boot 2 に Kotlin で入門してみた - 4. DB接続編
[3. GET パラメータ解析編](https://qiita.com/lasta/items/fcee2937405cbf5b9c1b) の続きになります。

新規でAPIサーバを作りたくなり、Spring Boot 2 を用いて作成してみます。
Kotlin Fest 2018で、Kotlin でも [Kotlin ネイティブなフレームワーク(ktorなど)][ktor] ではなくて [Java ネイティブなフレームワーク][Kotlin Fest 2018] を使うのがおすすめという話があったので、 Spring Boot 2 を選びました。

入門時の私のスキルは下記の通りです。

* Java : 2年半 (サーバサイド、コンバータ)
* Kotlin : 1年 (コンバータ)
* サーバサイドJava : 2年半 (独自フレームワーク)
    * JPA (Hibernate) を使用 (基本的に `CriteriaQuery` を使用)
* IntelliJ IDEA : 1年半
* Spring Boot : 未経験

上述の通り、 Spring Boot は未経験のため、誤りがある可能性があります。
誤りがあった際には、ご指摘いただけると嬉しいです!
一方で完全に未経験のため、同様にこれから始める方の参考になると思います。

目標は、 Solr を用いて検索した結果をもとに MySQL からデータを取得して返却する API を作成することとしました。

[ソースコードは Github に置きました][github]

[ktor]: https://qiita.com/lasta/items/2c25ae5a875ba8da4f8a
[Kotlin Fest 2018]: http://tech.connehito.com/entry/2018/08/31/131552
[github]: https://github.com/lasta/sample-spring-boot-kotlin/tree/qiita/4-db

## 参考書
* [Spring徹底入門 Spring FrameworkによるJavaアプリケーション開発](https://www.amazon.co.jp/gp/product/B01IEWNLBU/ref=oh_aui_d_detailpage_o01_?ie=UTF8&psc=1)

## Spring と DB
Spring はもちろん Java 製のフレームワークなので、 `java.sql.Connection` を用いて CRUD 操作を行うことができます。
ですが、昨今はそのような実装を行うことは減ってきており、 JPA 、 Hibernate 、 MyBatis などの ORM (Object-Relational Mapper; ORマッパー) を用いることが多いです。
今回は、 JPA + Hibernate を用いた実装を目指します。

## 前準備 (DBの作成)
DB へアクセスする以上、 DB そのものの構築が必要です。
まずはそれから行っていきます。
今回は、 MySQL で構築します。

### DB の構築
私は OS X を用いて開発をしているため、 [Homebrew](https://brew.sh/index_ja) を用いてローカル環境に構築します。
アプリケーションとは切り離された DB として構築するため、アプリケーションから疎通できる場所であればどこでも構いません。

```bash:インストールから起動まで
brew update
brew install mysql
brew service start mysql
```

ここまでで、 MySQL サーバが起動しているかと思います。
このあとは、アプリケーションから接続するためのユーザの作成、データベースの作成、テーブルの作成、データの投入を行います。
今回投入するデータは、日本郵便株式会社が提供する郵便番号データ (東京都のみ) です。
具体的なデータについては、データ投入までの流れで説明します。

### ユーザの作成
* ユーザ名 : `demo_user`
* パスワード : `demo_pass`
* 接続先DB : `zipcode`

もちろんこれらの値は任意になりますが、今後はこの権限情報を前提に解説します。

```sql:create_user.sql
CREATE USER IF NOT EXISTS 'demo_user'@'localhost'
  IDENTIFIED BY 'demo_pass'
  PASSWORD EXPIRE NEVER;

GRANT ALL ON zipcode.* TO 'demo_user'@'localhost';
```

### データベースの作成
郵便番号データベースを作成する予定のため、DB名は `zipcode` としました。

```sql:create_database.sql
CREATE DATABASE IF NOT EXISTS zipcode
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;
```

### テーブルの作成
郵便番号データを投入するテーブルを作成します。
各カラムの意味は下記の通りです。
元データの詳しい仕様は「[郵便番号データの説明](https://www.post.japanpost.jp/zipcode/dl/readme.html)」を参照してください。

| カラム名        | 説明                                                                       | 元データの項番 |
|-----------------|----------------------------------------------------------------------------|----------------|
| `id`            | Unique key; 郵便番号データにユニークなIDは存在しないため、自動付与する     |                |
| `jis`           | 市区町村JISコード (全国地方公共団体コード)                                 | 1              |
| `old_zip_code`  | 旧郵便番号 (本来は5桁だが、ゴミデータが混ざるため型は `VARCHAR(7)` とする) | 2              |
| `zip_code`      | 郵便番号 (半角数字7桁)                                                     | 3              |
| `province_ruby` | 都道府県名ルビ                                                             | 4              |
| `city_ruby`     | 市区町村名ルビ                                                             | 5              |
| `town_ruby`     | 町域名ルビ                                                                 | 6              |
| `province`      | 都道府県名                                                                 | 7              |
| `city`          | 市区町村名                                                                 | 8              |
| `town`          | 町域名                                                                     | 9              |

DBから取得する際は郵便番号を指定することを想定して、 `zip_code` にインデックスを張っています。

```sql:create_zipcode_table.sql
DROP TABLE IF EXISTS `zipcode`;
CREATE TABLE `zipcode` (
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `jis`           VARCHAR(5)   NOT NULL,
  `old_zip_code`  VARCHAR(7)   NOT NULL,
  `zip_code`      VARCHAR(7)   NOT NULL,
  `province_ruby` VARCHAR(255) NOT NULL,
  `city_ruby`     VARCHAR(255) NOT NULL,
  `town_ruby`     VARCHAR(255) NOT NULL,
  `province`      VARCHAR(255) NOT NULL,
  `city`          VARCHAR(255) NOT NULL,
  `town`          VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_zipcode` (`zip_code`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

### データの投入
日本郵便株式会社の[読み仮名データの促音・拗音を小書きで表記するもの(zip形式)][zipcode-list] の「東京都」のデータを一部加工したものを投入します。
つまり、東京都の郵便番号一覧です。
全件分投入するための SQL を作成しましたので、ご自由にご利用ください。
[sample-spring-boot-kotlin/src/main/resources/sql/insert_zipcode_table.sql](https://github.com/lasta/sample-spring-boot-kotlin/blob/master/src/main/resources/sql/insert_zipcode_table.sql)

[zipcode-list]: https://www.post.japanpost.jp/zipcode/dl/kogaki-zip.html

```bash:Insert_Data
mysql -uroot zipcode -e"$(cat insert_zipcode_table.sql)"
```

### データの確認
東京駅 (東京都千代田区丸の内1丁目) の郵便番号 100-0005 のデータを取得してみます。

```mysql:東京駅の郵便番号データを取得
lasta:~ $ mysql -u demo_user zipcode -p
Enter password:

mysql> select id, zip_code, province, city, town from zipcode where zip_code = '1000005';
+-----+----------+-----------+--------------+--------------------------------------+
| id  | zip_code | province  | city         | town                                 |
+-----+----------+-----------+--------------+--------------------------------------+
| 178 | 1000005  | 東京都    | 千代田区     | 丸の内（次のビルを除く）             |
+-----+----------+-----------+--------------+--------------------------------------+
1 row in set (0.00 sec)
```

大丈夫ですね。
これで事前準備は完了です。

## DB への接続
DB の構築ができたため、アプリケーションから DB へ接続してみます。

### DB へ接続するためのライブラリを導入
[初回](https://qiita.com/lasta/items/4279a01f205f9cc5e0d4) で [一時的にJPA,MySQLを用いないようにしてしまった](https://qiita.com/lasta/items/4279a01f205f9cc5e0d4#sql-%E3%82%AB%E3%83%86%E3%82%B4%E3%83%AA%E3%81%AE%E3%83%A9%E3%82%A4%E3%83%96%E3%83%A9%E3%83%AA%E3%82%92%E4%BD%BF%E7%94%A8%E3%81%99%E3%82%8B%E5%A0%B4%E5%90%88) ため、再度用いるようにします。
また、 Hibernate の設定が抜けていたため、追加しました。

```diff:build.gradle
  dependencies {
      compile('org.springframework.boot:spring-boot-starter-actuator')
      compile('org.springframework.boot:spring-boot-starter-data-elasticsearch')
- //    compile('org.springframework.boot:spring-boot-starter-data-jpa')
+     compile('org.springframework.boot:spring-boot-starter-data-jpa')
      compile('org.springframework.boot:spring-boot-starter-data-rest')
      compile('org.springframework.boot:spring-boot-starter-data-solr')
      compile('org.springframework.boot:spring-boot-starter-integration')
      compile('org.springframework.boot:spring-boot-starter-thymeleaf')
      compile('org.springframework.boot:spring-boot-starter-web')
      compile('com.fasterxml.jackson.module:jackson-module-kotlin')
      compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
      compile("org.jetbrains.kotlin:kotlin-reflect")
      runtime('org.springframework.boot:spring-boot-devtools')
+     compile("org.hibernate:hibernate-java8")
- //    runtime('mysql:mysql-connector-java')
+     runtime('mysql:mysql-connector-java')
      compileOnly('org.springframework.boot:spring-boot-configuration-processor')
      compileOnly('org.projectlombok:lombok')
      testCompile('org.springframework.boot:spring-boot-starter-test')
      testCompile('org.springframework.restdocs:spring-restdocs-mockmvc')
  }
```

加えて、 ビルドスクリプトにも追加します。
Kotlin には data class が存在するため、 Entity クラス (DB から取得したデータを格納するデータクラス) と相性が良いです。
ですが、 JPA では Entity クラスがデフォルトコンストラクタを持つ必要があります。
data class はプロパティすべてを引数とするコンストラクタしか持つことができません。
この制約を取り払うために、 [`kotlin-noarg`](https://kotlinlang.org/docs/reference/compiler-plugins.html) を導入します。

```groovy:build.gradle
// 必要な部分のみ抜粋しています
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-noarg:${kotlinVersion}")
    }
}
```

### DB へのアクセス方法
DB へのアクセス方法はいくつかあります。

* `java.sql.Connection`
    * 生 SQL を用いる、最も原始的な方法
    * 複雑な SQL を書かなければならない場合等でも対応できる
* JPA + JPQL (Java Persistence Query Language)
    * JPA 独自拡張の SQL を用いる方法
    * Entity クラスにて定義したプロパティ名を用いる
* JPA + Criteria Query
    * JPQL 相当のクエリを Java API 化したもの
    * Java で記述するため型検査等が可能になり、ある程度コンパイル時に記述ミスを検出できる
    * RDBMS 間の実装差分を吸収してくれるメリットもある
* JPA + Native Query
    * 生 SQL を用いる、 `java.sql.Connection` と同等の方法
    * 性能を始めとした何らかの理由により、特定の RDBMS 独自の機能を利用する場合に用いることがある
* Spring Data JPA + `JpaRepository`
    * DAO 層である Repository のインタフェースを作成しアノテーションやメソッドの定義をする (実装不要) だけで、 Entity の CRUD 操作ができるようになる
    * 前述の方法に比べてクエリの柔軟性は低い
    * `EntityManager` を隠蔽しているため、 Entity の管理状態や分離状態などを意識せずに操作できる
* Spring Data JPA + JPQL
    * 上述の `JpaRepository` で不足するクエリを生成できる
        * クエリはアノテーション、またはメソッド名 + 引数名で宣言的に指定
    * 参照系のクエリのみ記述可能
* Spring Data JPA + Criteria Query
    * JpaRepository に対し独自メソッドを定義することで、 Spring Data JPA + JPQL 等に比べてより柔軟なクエリを生成できる
    * 動的にクエリの生成等が必要な場合に用いる

今回は Spring Data JPA を主軸とします。

### 接続情報の定義

接続情報の記述は、アプリケーションの設定ファイルに行います。
配置場所は `src/main/resources/application.properties` になります。
Spring Initializr を用いてプロジェクトを作成した場合、空の `application.properties` が生成されています。
この設定は、プロパティ形式またはYAML形式で行います。
どちらのフォーマットを用いるかどうかは特に設定の必要はなく、 `application.properties` の代わりに `application.yml` を配置するだけでOKです。
今回はYAML形式で記述します。
設定値に関しては、「[Kotlin with Spring Boot 1.5で簡単なRest APIを実装する](https://qiita.com/rubytomato@github/items/7d4bb10ca3779ab3277c)」 を参考にさせていただきました。

重要な部分のみ抜粋します。

```src/main/resources/application.yml:datasource
spring:
  # データソースの設定
  datasource:
    # JDBC接続URLの指定
    url: jdbc:mysql://localhost:3306/zipcode?useSSL=false
    # DB接続時のユーザ名
    username: demo_user
    # DB接続時のパスワード
    password: demo_pass
    # JDBC Driver
    driver-class-name: com.mysql.jdbc.Driver
```

### アプリケーションの実装 (Spring Data JPA + `JpaRepository`)
Spring Data JPA と `JpaRepository` を用いたパターンを紹介します。

#### パッケージ構成
```
com.lasta.api.sample.
├── SampleApplication.kt
├── configuration
│   └── DataSourceConfiguration.kt
├── controller
│   └── ZipCodeController.kt
├── entity
│   └── ZipCodeEntity.kt
├── repository
│   └── ZipCodeRepository.kt
└── service
    ├── ZipCodeService.kt
    └── impl
        └── ZipCodeServiceImpl.kt
```

#### Controller
今回作成する API は、郵便番号APIです。
郵便番号を指定したら、郵便番号の住所が返却されます。

* path : `/zipcode`
* parameter
    * `code` (必須)
        * 7桁で郵便番号を指定 (ハイフンなし)
        * String 型 (7文字限定)
        * デフォルト値なし
* response code
    * 値を取得できた場合 200
    * 値を取得できなかった場合 (指定した郵便番号が存在しない場合) 404
        * 本来は 200 で response body を空の配列を返却するべき
* response body
    * JSON 形式
    * `ZipCodeEntity` の配列

```ZipCodeController.kt
package com.lasta.api.sample.controller

import com.lasta.api.sample.entity.ZipCodeEntity
import com.lasta.api.sample.service.ZipCodeService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.Size

@RestController
@RequestMapping(path = ["zipcode"])
class ZipCodeController(private val service: ZipCodeService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getByZipCode(
            @Size(message = "code must be 7 letters.", max = 7, min = 7)
            @RequestParam(name = "code", required = true)
            zipCode: String
    ): ResponseEntity<Collection<ZipCodeEntity>> {
        val zipCodeEntities: Collection<ZipCodeEntity> = service.findByZipCode(zipCode)

        if (zipCodeEntities.isEmpty()) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity.ok(zipCodeEntities)
    }
}
```

#### Service
郵便番号 (`zipCode`) を受け取ってその郵便番号の情報のコレクション `Collection<ZipCodeEntity>` を返却します。
今回は Service 層では特別な処理はしないため、ほぼ DAO 層への橋渡しのみになっています。
後述の `ZipCodeRepositoryImpl` は DI コンテナで管理されています。

```ZipCodeService.kt
package com.lasta.api.sample.service

import com.lasta.api.sample.entity.ZipCodeEntity

interface ZipCodeService {
    fun findByZipCode(zipCode: String): Collection<ZipCodeEntity>
}
```

```ZipCodeServiceImpl.kt
package com.lasta.api.sample.service.impl

import com.lasta.api.sample.entity.ZipCodeEntity
import com.lasta.api.sample.repository.ZipCodeRepository
import com.lasta.api.sample.service.ZipCodeService
import org.springframework.stereotype.Service

@Service
class ZipCodeServiceImpl(private val repository: ZipCodeRepository) : ZipCodeService {
    override fun findByZipCode(zipCode: String): Collection<ZipCodeEntity> = repository.findByZipCode(zipCode)
}
```

#### Configuration
Repository 層の前に、 Spring Data JPA の設定をします。

Spring Data JPA に限らず、 JPA ではエンティティを管理する `EntityManager` を作成する際に、 `EntityManagerFactory` を使用します。
Spring Data JPA ではこれを DI コンテナ上で扱う必要があるため、 DI 上に `EntityManagerFactory` が作成されるようにします。

```kotlin:DataSourceConfiguration.kt
package com.lasta.api.sample.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.Database
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("com.lasta.api.sample.repository")
class DataSourceConfiguration {

    @Autowired
    lateinit var dataSource: DataSource

    @Bean
    fun jpaVendorAdapter(): JpaVendorAdapter =
            HibernateJpaVendorAdapter().apply {
                setDatabase(Database.MYSQL)
                setShowSql(true)
            }

    @Bean
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean =
            LocalContainerEntityManagerFactoryBean().apply {
                setDataSource(dataSource)
                setPackagesToScan("com.lasta.api.sample.entity")
                this.jpaVendorAdapter = jpaVendorAdapter()
            }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager =
            JpaTransactionManager().apply {
                setEntityManagerFactory(entityManagerFactory)
            }
}
```

##### `DataSourceConfiguration` クラスのアノテーション
* `@Configuration`
    * DI コンテナに登録されるアノテーションのひとつ
* `@EnableTransactionManagement`
    * トランザクション管理を有効化するアノテーション
    * JPA のトランザクション管理をしたいメソッドに `@Transactional` アノテーションをつけるだけで管理できるようになる
* `@EnableJpaRepositories`
    * Spring Data JPA を有効化する
    * 指定したパッケージ配下の Repository インタフェースに対し有効化する
        * Repository インタフェースについては後述

##### `DataSourceConfiguration` の各メソッド
各種設定を行うメソッド群です。
`persitence.xml` の代わりになります。
それぞれ `@Bean` アノテーションを付与することで、 Bean として DI コンテナに登録します。

* `jpaVendorAdapter`
    * 各 JPA 実装特有の設定を行う
    * `HibernateJpaVendorAdapter`
        * どの JPA 実装を用いるか定義する
    * `setDatabase(Database.MYSQL)`
        * 使用する RDBMS を指定
    * `setShowSql(true)`
        * Hibernate が生成した SQL をログに出力するようにする
* `entityManagerFactory`
    * JPA 実装に依らない共通の EntityManager の設定を行う
    * `setPackagesToScan`
        * `Entity` クラスが定義されているパッケージを指定する
            * Entity クラスについては後述
        * 指定されたパッケージ配下の `Entity` クラスのみを `EntityManager` が扱う
    * `jpaVendorAdapter`
        * 先に定義した `JpaVendorAdapter` を指定する
          * 今回は Hibernate による実装を用いる
* `transactionManager`
    * `@EnableTransactionManagement` を用いるための設定

これで、 `JpaRepository` が CRUD 操作ができるようになりました。

#### Entity
DB から取得した値を格納するデータクラスです。
Kotlin は data class があるため、それを用います。

```kotlin:ZipCodeEntity.kt
package com.lasta.api.sample.entity

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "zipcode")
data class ZipCodeEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = -1,
        @Column(name = "jis", nullable = false, insertable = false, updatable = false)
        var jis: String = "",
        @Column(name = "old_zip_code", nullable = false, insertable = false, updatable = false)
        var oldZipCode: String = "",
        @Column(name = "zip_code", nullable = false, insertable = false, updatable = false)
        var zipCode: String = "",
        @Column(name = "province_ruby", nullable = false, insertable = false, updatable = false)
        var provinceRuby: String = "",
        @Column(name = "city_ruby", nullable = false, insertable = false, updatable = false)
        var cityRuby: String = "",
        @Column(name = "town_ruby", nullable = false, insertable = false, updatable = false)
        var townRuby: String = "",
        @Column(name = "province", nullable = false, insertable = false, updatable = false)
        var provinceName: String = "",
        @Column(name = "city", nullable = false, insertable = false, updatable = false)
        var cityName: String = "",
        @Column(name = "town", nullable = false, insertable = false, updatable = false)
        var townName: String = ""
) : Serializable
```

* `@Entity`
    * このクラスが Entity クラスであることを示す
* `@Table`
    * この Entity クラスは `zipcode` テーブルの Entity であることを指定する
* `@Id`
    * 主キーであることの宣言
    * 複合主キーの場合は、別途複合主キーのみのデータクラスを作成の上、そのデータクラス型のプロパティに `@EmbeddedId` アノテーションを付与する
* `@GeneratedValue(strategy = GenerationType.IDENTITY)`
    * 値の生成を JPA に任せる
      * [「@GeneratedValueを使って主キーを生成する方法」](https://qiita.com/KevinFQ/items/a6d92ec7b32911e50ffe) が詳しいです。
* `@Column`
    * DB のカラムと Kotlin のプロパティをマッピングする

#### Repository
これまでの定義により、 `JpaRepository` を継承した `Repository` インタフェースのメソッドにアクセスするだけで、 JPA  により DB にアクセスすることができます。

今回実行したい SQL は下記になります。

```sql
SELECT *
FROM zipcode
WHERE zip_code = :zipCode; -- `:zipCode` は何らかの値を指定する
```

Spring Data JPA では JPQL でデータをアクセスする方法が大きく分けて2つあります。
1つ目は `@Query` アノテーションを用いて、アノテーションに引数に直接 JPQL を記述する方法です。

```kotlin:@Queryアノテーションの使用例
interface ZipCodeRepository : JpaRepository<ZipCodeEntity, Long> {
    @Query("SELECT z FROM zipcode z WHERE z.zip_code = :zipCode")
    fun findByZipCode(@Param("zipCode") zipCode: String): List<ZipCodeEntity>
}
```

JPQL 内の `:zipCode` は、対応する値が付与された `@param` アノテーションを持つメソッド引数の値が格納されます。
`@Query` に記述する JPQL は Spring Data JPA 独自の拡張がされています。
詳しくは [Spring Data JPA - Reference Documentation 5.3.4. Using `@Query`](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.at-query) を参照してください。

2つ目は、 Repository インタフェースのメソッド名からクエリを生成する方法です。
制約は強いですが、単純なクエリであればメソッドを定義するだけでクエリを生成できるため、便利です。
今回はこちらを採用しました。

```kotlin:ZipCodeRepository(メソッド名からクエリを生成)
package com.lasta.api.sample.repository

import com.lasta.api.sample.entity.ZipCodeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ZipCodeRepository : JpaRepository<ZipCodeEntity, Long> {
    fun findByZipCode(zipCode: String): List<ZipCodeEntity>
}
```

`fun findByXxx(xxx: T): Collection<R>` と定義するだけで、 `SELECT R FROM R WHERE R.xxx = :xxx` が生成されます。

他にも`fun findByXxxAndYyy(xxx: T, yyy: U): Collection<R>` ならば `SELECT R FROM R WHERE R.xxx = :xxx AND R.yyy = :yyy` など、自然言語風に記述することで様々なクエリを自動生成できます。

サポートされているキーワードについては [Spring Data JPA - Reference Documentation 5.3.2 Query Creation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation) に記載があります。

#### 単体テスト
<!-- TODO -->

### 他の実装方法 (Repository へ独自のクエリを追加)
<!-- TODO -->
(後日追加します)

## まとめ
Spring Data JPA + `JpaRepository` を用いた方法で DB アクセスを行う方法を紹介しました。
これ以外にも様々な方法で実装することができます。
DAO 層は煩雑化しやすいため、どのような SQL を実行するのか ( 結合やサブクエリ等は使用する? 複数のデータソースを用いる?) を予め定め、それに応じた設計をする必要があります。

## 次回
Solr からデータを取得 (予定)
