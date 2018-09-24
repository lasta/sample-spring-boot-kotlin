# Spring Boot 2 に Kotlin で入門してみた - 4. DB接続編
[3. GET パラメータ解析編](https://qiita.com/lasta/items/fcee2937405cbf5b9c1b) の続きになります。

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

```bash:Insert_Data
# insert_zipcode_table.sql locates `src/main/resources/sql/insert_zipcode_table.sql`
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
