# 郵便番号データ `13-tokyo.csv`
日本郵便株式会社の[読み仮名データの促音・拗音を小書きで表記するもの(zip形式)][zipcode-list] の「東京都」のデータを用い、一部加工を行いました。
[zipcode-list]: https://www.post.japanpost.jp/zipcode/dl/kogaki-zip.html

## Solr への登録
```bash
# Install Solr
brew install solr
# Run Solr
brew services start solr
# Add core 'zipcode'
cd /usr/local/Cellar/solr/7.4.0/server/solr
mkdir zipcode
cp -r configsets/_default/conf zipcode
curl -s 'http://localhost:8983/solr/admin/cores?action=CREATE&name=zipcode&instanceDir=zipcode&config=solrconfig.xml&dataDir=data'
# Import zipcode data into 'zipcode' core
cd ~/repos/sample-spring-boot-kotlin/data
curl 'http://localhost:8983/solr/zipcode/update?commit=true&indent=true&wt=json' --data-binary @./13-tokyo.csv -H 'Content-Type: text/csv'
# Test to search with Solr (numFound : 485)
http://localhost:8983/solr/zipcode/select?fl=jis_s,old_zip_code_s,zip_code_s,province_ruby_txt_ja,city_ruby_txt_ja,town_ruby_txt_ja,province_txt_ja,city_txt_ja,town_txt_ja&q=city_txt_ja:%E5%8D%83%E4%BB%A3%E7%94%B0&wt=json
```

## DB への登録
```bash:install_MySQL
# Install DB
brew install mysql
# Run DB
brew services start mysql
# Run MySQL client
mysql -u root
```

```sql:create_user.sql
CREATE USER IF NOT EXISTS 'demo_user'@'localhost'
  IDENTIFIED BY 'demo_pass'
  PASSWORD EXPIRE NEVER;

GRANT ALL ON zipcode.* TO 'demo_user'@'localhost';
```

```sql:create_database.sql
CREATE DATABASE IF NOT EXISTS zipcode
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;
```

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

```bash:Insert_Data
# insert_zipcode_table.sql locates `src/main/resources/sql/insert_zipcode_table.sql`
mysql -uroot zipcode -e"$(cat insert_zipcode_table.sql)"
```



