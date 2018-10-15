package com.lasta.api.sample.entity

import org.apache.solr.client.solrj.beans.Field
import org.springframework.data.solr.core.mapping.SolrDocument

@SolrDocument(collection = "zipcode")
data class ZipCodeSolrEntity(
    @Field("jis_s")
    val jisCode: String,
    @Field("old_zip_code_s")
    val oldZipCode: String,
    @Field("zip_code_s")
    val zipCode: String,
    @Field("province_ruby_txt_ja")
    val provinceRuby: String,
    @Field("city_ruby_txt_ja")
    val cityRuby: String,
    @Field("town_ruby_txt_ja")
    val townRuby: String,
    @Field("province_txt_ja")
    val province: String,
    @Field("city_txt_ja")
    val city: String,
    @Field("town_txt_ja")
    val town: String,
    @Field("id")
    val id: String
)