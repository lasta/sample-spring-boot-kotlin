package com.lasta.api.sample.configuration

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