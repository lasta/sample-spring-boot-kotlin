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
