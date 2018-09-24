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