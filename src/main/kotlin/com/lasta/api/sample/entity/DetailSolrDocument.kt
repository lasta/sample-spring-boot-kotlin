package com.lasta.api.sample.entity

import com.lasta.api.sample.entity.DetailSolrField.COL_ID
import com.lasta.api.sample.entity.DetailSolrField.ID
import com.lasta.api.sample.entity.DetailSolrField.NAME
import com.lasta.api.sample.entity.DetailSolrField.PROVIDER_ID
import com.lasta.api.sample.entity.DetailSolrField.SPOT_ID
import com.lasta.api.sample.entity.DetailSolrField.TEXT
import org.springframework.data.annotation.Id
import org.springframework.data.solr.core.mapping.Indexed
import org.springframework.data.solr.core.mapping.SolrDocument

@SolrDocument(solrCoreName = "detail")
data class DetailSolrDocument(
        @Id @Indexed(ID) val id: String,
        @Indexed(PROVIDER_ID) val providerId: String,
        @Indexed(SPOT_ID) val spotId: String,
        @Indexed(NAME) val name: String,
        @Indexed(COL_ID) val columnId: String,
        @Indexed(TEXT) val text: String
) {
    override fun toString(): String =
            "[detail]: [id = $id, code = $providerId-$spotId, name = $name]"
}
