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

