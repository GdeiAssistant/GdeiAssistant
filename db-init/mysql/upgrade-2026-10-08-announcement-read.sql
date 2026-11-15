-- Apply to the configured data database before deploying the new announcement unread endpoints.
SET @target_db = IF(DATABASE() IS NULL OR DATABASE() = '', 'gdeiassistant_data', DATABASE());
SET @sql = CONCAT(
  'CREATE TABLE IF NOT EXISTS `', REPLACE(@target_db, '`', '``'), '`.`announcement_read` (',
  '  username VARCHAR(24) COLLATE utf8mb4_bin NOT NULL,',
  '  announcement_id INT NOT NULL,',
  '  PRIMARY KEY (username, announcement_id),',
  '  CONSTRAINT fk_announcement_read FOREIGN KEY (announcement_id) REFERENCES `', REPLACE(@target_db, '`', '``'), '`.`announcement`(id) ON DELETE CASCADE',
  ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
