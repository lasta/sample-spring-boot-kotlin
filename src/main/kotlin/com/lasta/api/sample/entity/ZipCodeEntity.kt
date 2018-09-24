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
        val id: Long,
        @Column(name = "jis", nullable = false, insertable = false, updatable = false)
        val jis: String,
        @Column(name = "old_zip_code", nullable = false, insertable = false, updatable = false)
        val oldZipCode: String,
        @Column(name = "zip_code", nullable = false, insertable = false, updatable = false)
        val zipCode: String,
        @Column(name = "province_ruby", nullable = false, insertable = false, updatable = false)
        val provinceRuby: String,
        @Column(name = "city_ruby", nullable = false, insertable = false, updatable = false)
        val cityRuby: String,
        @Column(name = "townRuby", nullable = false, insertable = false, updatable = false)
        val townRuby: String,
        @Column(name = "province", nullable = false, insertable = false, updatable = false)
        val provinceName: String,
        @Column(name = "city", nullable = false, insertable = false, updatable = false)
        val cityName: String,
        @Column(name = "town", nullable = false, insertable = false, updatable = false)
        val townName: String
) : Serializable